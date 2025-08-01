/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.tilemap;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2i;
import org.tinylog.Logger;

import java.util.*;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.lib.tilemap.TerrainTile.*;
import static java.lang.Math.signum;

/**
 * Open or closed polygon. Corners are represented by two diagonal vectors.
 */
public class Obstacle {
    private final Vector2i startPoint;
    private final List<ObstacleSegment> segments = new ArrayList<>();
    private List<RectShort> innerAreaRectangles = List.of();

    public Obstacle(Vector2i startPoint) {
        this.startPoint = Objects.requireNonNull(startPoint);
    }

    public void addSegment(Vector2i vector, boolean counterClockwise, byte content) {
        Objects.requireNonNull(vector);
        segments.add(new ObstacleSegment(endPoint(), vector, counterClockwise, content));
        if (isClosed()) {
            try {
                Collection<Vector2i> innerPolygon = computeInnerPolygon();
                PolygonToRectangleConverter<RectShort> converter = RectShort::new;
                innerAreaRectangles = converter.convertPolygonToRectangles(innerPolygon);
            } catch (Exception x) {
                Logger.warn("Inner area rectangle partition could not be computed");
                Logger.error(x);
            }
        }
    }

    public List<RectShort> innerAreaRectangles() {
        return Collections.unmodifiableList(innerAreaRectangles);
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
            "start=" + startPoint +
            ", segment count=" + segments.size() +
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

    public Vector2i endPoint() {
        return segments.isEmpty()
            ? startPoint
            : segments.getLast().startPoint().plus(segments.getLast().vector());
    }

    public Vector2i point(int i) {
        return i < numSegments() ? segment(i).startPoint() : segment(i).endPoint();
    }

    public boolean isClosed() {
        return startPoint().equals(endPoint());
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

    public Vector2i cornerCenter(int segmentIndex) {
        ObstacleSegment corner = segment(segmentIndex);
        byte code = corner.encoding();
        if (code == ARC_NW.$) return point(segmentIndex).plus(0, HTS);
        if (code == ARC_SW.$) return point(segmentIndex).plus(HTS, 0);
        if (code == ARC_SE.$) return point(segmentIndex).plus(0, -HTS);
        if (code == ARC_NE.$) return point(segmentIndex).plus(-HTS, 0);
        throw new IllegalStateException("No corner tile at index " + segmentIndex);
    }

    public Vector2i[] cornerCenters() {
        var centers = new LinkedHashSet<Vector2i>();
        for (ObstacleSegment segment : segments) {
            boolean up = segment.vector().y() < 0, down = segment.vector().y() > 0;
            byte code = segment.encoding();
            if (code == ARC_NW.$ && down) centers.add(segment.startPoint().plus(0, HTS));
            if (code == ARC_SW.$ && down) centers.add(segment.startPoint().plus(HTS, 0));
            if (code == ARC_SE.$ && up)   centers.add(segment.startPoint().plus(0, -HTS));
            if (code == ARC_NE.$ && up)   centers.add(segment.startPoint().plus(-HTS, 0));
        }
        return centers.toArray(Vector2i[]::new);
    }

    public Collection<Vector2i> computeInnerPolygon() {
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
                Vector2i e = segment.isNWCorner() || segment.isSECorner() ? Vector2i.of(0, v.y()) : Vector2i.of(v.x(), 0);
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
}