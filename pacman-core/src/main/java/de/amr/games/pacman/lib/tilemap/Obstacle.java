/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2i;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.vec_2i;
import static java.lang.Math.signum;

/**
 * Open or closed polygon. Corners are represented by two diagonal vectors.
 */
public class Obstacle {

    private final Vector2i startPoint;
    private Vector2i endPoint;
    private final List<ObstacleSegment> segments = new ArrayList<>();
    private final boolean doubleWalls;
    private final List<RectArea> rectPartition;

    public Obstacle(Vector2i startPoint, boolean doubleWalls) {
        this.startPoint = this.endPoint = Objects.requireNonNull(startPoint);
        this.doubleWalls = doubleWalls;
        rectPartition = new ArrayList<>();
    }

    public void addSegment(Vector2i vector, boolean ccw, byte content) {
        ObstacleSegment segment = new ObstacleSegment(endPoint, vector, ccw, content);
        segments.add(segment);
        endPoint = segment.endPoint();
        if (isClosed()) {
            rectPartition.clear();
            try {
                Collection<Vector2i> innerPolygon = computeInnerPolygonPoints();
                rectPartition.addAll(GourleyGreenPolygonToRect.convertPolygonToRectangles(innerPolygon));
            } catch (Exception x) {
                Logger.warn("Inner area rectangle covering could not be computed yet");
            }
        }
    }

    public Stream<RectArea> rectPartition() {
        return rectPartition.stream();
    }

    public Vector2i[] points() {
        List<Vector2i> points = new ArrayList<>();
        points.add(startPoint);
        for (ObstacleSegment segment : segments) {
            points.add(segment.startPoint().plus(segment.vector()));
        }
        return points.toArray(Vector2i[]::new);
    }

    @Override
    public String toString() {
        return "Obstacle{" +
            "encoding=" + encoding() +
            ", start=" + startPoint +
            ", end=" + endPoint +
            ", doubleWalls=" + doubleWalls +
            ", rectangles=" + rectPartition +
            ", segment count=" + segments.size() +
            ", segments=" + segments +
            '}';
    }

    public String encoding() {
        StringBuilder encoding = new StringBuilder();
        for (int i = 0; i < segments.size(); ++i) {
            byte tileCode = segment(i).encoding();
            char ch = (char) ('a' + tileCode);
            encoding.append(ch);
        }
        return encoding.toString();
    }

    public Vector2i startPoint() { return startPoint; }

    public Vector2i endPoint() { return endPoint; }

    public Vector2i point(int i) {
        return i < numSegments() ? segment(i).startPoint() : segment(i).endPoint();
    }

    public boolean isClosed() {
        return startPoint().equals(endPoint());
    }

    public boolean hasDoubleWalls() {
        return doubleWalls;
    }

    public List<ObstacleSegment> segments() {
        return Collections.unmodifiableList(segments);
    }

    public int numSegments() {
        return segments.size();
    }

    public ObstacleSegment segment(int i) {
        return segments.get(i);
    }

    public IntStream uTurnSegmentIndices() {
        return IntStream.range(0, segments.size()).filter(this::hasUTurnAt);
    }

    private boolean hasUTurnAt(int i) {
        ObstacleSegment segment = segments.get(i);
        ObstacleSegment nextSegment = i < segments.size() - 1 ? segments.get(i + 1) : segments.getFirst();
        return isUTurn(segment, nextSegment);
    }

    /** Tells if two corner segments in counter-clockwise order form a U-turn */
    private boolean isUTurn(ObstacleSegment s1, ObstacleSegment s2) {
        if (s1.isRoundedNECorner() && s2.isRoundedNWCorner()) return true;
        if (s1.isRoundedNWCorner() && s2.isRoundedSWCorner()) return true;
        if (s1.isRoundedSWCorner() && s2.isRoundedSECorner()) return true;
        if (s1.isRoundedSECorner() && s2.isRoundedNECorner()) return true;
        if (s1.isAngularNECorner() && s2.isAngularNWCorner()) return true;
        if (s1.isAngularNWCorner() && s2.isAngularSWCorner()) return true;
        if (s1.isAngularSWCorner() && s2.isAngularSECorner()) return true;
        if (s1.isAngularSECorner() && s2.isAngularNECorner()) return true;
        return false;
    }

    public Vector2i cornerCenter(int segmentIndex) {
        ObstacleSegment corner = segment(segmentIndex);
        return switch (corner.encoding()) {
            case TileEncoding.CORNER_NW -> point(segmentIndex).plus(0, HTS);
            case TileEncoding.CORNER_SW -> point(segmentIndex).plus(HTS, 0);
            case TileEncoding.CORNER_SE -> point(segmentIndex).plus(0, -HTS);
            case TileEncoding.CORNER_NE -> point(segmentIndex).plus(-HTS, 0);
            default -> throw new IllegalStateException("No corner tile at index " + segmentIndex);
        };
    }

    public Vector2i[] cornerCentersAtSegments(int... segmentIndices) {
        return Arrays.stream(segmentIndices).mapToObj(this::cornerCenter).toArray(Vector2i[]::new);
    }

    public Vector2i[] cornerCenters() {
        var centers = new LinkedHashSet<Vector2i>();
        for (var segment : segments) {
            boolean up = segment.vector().y() < 0, down = segment.vector().y() > 0;
            switch (segment.encoding()) {
                case TileEncoding.CORNER_NW, TileEncoding.DCORNER_NW -> {
                    if (down) centers.add(segment.startPoint().plus(0, HTS));
                }
                case TileEncoding.CORNER_SW, TileEncoding.DCORNER_SW -> {
                    if (down) centers.add(segment.startPoint().plus(HTS, 0));
                }
                case TileEncoding.CORNER_SE, TileEncoding.DCORNER_SE -> {
                    if (up) centers.add(segment.startPoint().plus(0, -HTS));
                }
                case TileEncoding.CORNER_NE, TileEncoding.DCORNER_NE -> {
                    if (up) centers.add(segment.startPoint().plus(-HTS, 0));
                }
            }
        }
        return centers.toArray(Vector2i[]::new);
    }

    public Collection<Vector2i> computeInnerPolygonPoints() {
        Vector2i start = startPoint();
        List<Vector2i> edges1 = replaceDiagonalCornerEdges();
        List<Vector2i> edges2 = removeInversePairs(edges1);
        // Handle degenerate case
        if (edges2.size() > 2) {
            if (edges2.getLast().equals(edges2.getFirst().inverse())) {
                edges2.removeLast();
                edges2.removeLast();
                start = start.plus(edges2.removeFirst());
                start = start.plus(edges2.removeFirst());
            }
        }
        List<Vector2i> edges3 = combineEdgesWithSameDirection(edges2);
        return makeOpenPolygonPoints(start, edges3);
    }

    private List<Vector2i> replaceDiagonalCornerEdges() {
        var edges = new ArrayList<Vector2i>();
        for (ObstacleSegment segment : segments) {
            Vector2i v = segment.vector();
            if (v.x() != 0 && v.y() != 0) { // diagonal
                Vector2i e = segment.isNWCorner() || segment.isSECorner() ? vec_2i(0, v.y()) : vec_2i(v.x(), 0);
                edges.add(e);
                edges.add(v.minus(e));
            } else {
                edges.add(v);
            }
        }
        return edges;
    }

    private List<Vector2i> removeInversePairs(List<Vector2i> polygonEdges) {
        var stack = new ArrayDeque<Vector2i>();
        for (Vector2i edge : polygonEdges) {
            if (stack.isEmpty()) {
                stack.push(edge);
            } else {
                if (stack.peek().equals(edge.inverse())) {
                    stack.pop();
                } else {
                    stack.push(edge);
                }
            }
        }
        return new ArrayList<>(stack.reversed()); // stack.reversed() returns immutable list
    }

    private List<Vector2i> combineEdgesWithSameDirection(List<Vector2i> polygonEdges) {
        var edges = new ArrayList<Vector2i>();
        if (polygonEdges.isEmpty()) {
            return edges;
        }
        edges.add(polygonEdges.getFirst());
        for (int i = 1; i < polygonEdges.size(); ++i) {
            Vector2i edge = polygonEdges.get(i);
            Vector2i last = edges.getLast();
            if (sameDirection(edge, last)) {
                edges.removeLast();
                edges.add(last.plus(edge));
            } else {
                edges.add(edge);
            }
        }
        return edges;
    }

    private List<Vector2i> makeOpenPolygonPoints(Vector2i startPoint, List<Vector2i> polygonEdges) {
        var points = new ArrayList<Vector2i>();
        points.add(startPoint);
        for (Vector2i edge : polygonEdges) {
            points.add(points.getLast().plus(edge));
        }
        if (points.getFirst().equals(points.getLast())) {
            points.removeLast();
        }
        return points;
    }

    private boolean sameDirection(Vector2i v, Vector2i w) {
        return signum(v.x()) == signum(w.x()) && signum(v.y()) == signum(w.y());
    }

    // experimental

    public void checkParent(Obstacle other) {
        for (RectArea parentRect : other.rectPartition) {
            for (RectArea childRect : rectPartition) {
                if (parentRect.contains(childRect)) {
                    parent = other;
                    Logger.info("Obstacle {} at {} is contained in obstacle {} at {}",
                            encoding(), startPoint, other.encoding(), other.startPoint);
                }
            }
        }
    }

    public boolean isContainedIn(Obstacle other) {
        return other == parent;
    }

    private Obstacle parent;

    public Obstacle getParent() {
        return parent;
    }
}