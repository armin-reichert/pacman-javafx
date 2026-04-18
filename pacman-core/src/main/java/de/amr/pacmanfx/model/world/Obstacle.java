/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.world;

import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.lib.PolygonToRectangleConverter;
import org.tinylog.Logger;

import java.util.*;

import static de.amr.basics.math.Vector2i.vec2_int;
import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.model.world.TerrainTile.*;
import static java.lang.Math.signum;

/**
 * Open or closed polygon. Corners are represented by two diagonal vectors.
 */
public class Obstacle {
    private final Vector2i startPoint;
    private final List<ObstacleSegment> segments = new ArrayList<>();
    private List<RectShort> innerAreaRectangles;

    private boolean borderObstacle;

    public Obstacle(Vector2i startPoint) {
        this.startPoint = Objects.requireNonNull(startPoint);
    }

    public void setBorderObstacle(boolean borderObstacle) {
        this.borderObstacle = borderObstacle;
    }

    public boolean borderObstacle() {
        return borderObstacle;
    }

    public void addSegment(Vector2i vector, boolean counterClockwise, byte content) {
        Objects.requireNonNull(vector);
        segments.add(new ObstacleSegment(endPoint(), vector, counterClockwise, content));
        innerAreaRectangles = null; // force recomputation when queried
    }

    public List<RectShort> innerAreaRectangles() {
        if (innerAreaRectangles == null) {
            if (isClosed()) {
                try {
                    Collection<Vector2i> innerPolygon = computeInnerPolygon();
                    PolygonToRectangleConverter<RectShort> converter = RectShort::of;
                    innerAreaRectangles = converter.convertPolygonToRectangles(innerPolygon);
                } catch (Exception x) {
                    Logger.warn(x, "Inner area rectangle partition could not be computed");
                }
            }
        }
        return innerAreaRectangles == null ? List.of() : Collections.unmodifiableList(innerAreaRectangles);
    }

    public List<Vector2i> points() {
        final List<Vector2i> points = new ArrayList<>();
        points.add(startPoint);
        for (ObstacleSegment segment : segments) {
            points.add(segment.startPoint().plus(segment.vector()));
        }
        return points;
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

    public boolean isClosed() {
        return startPoint.equals(endPoint());
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

    public List<Vector2f> cornerCenterPoints() {
        final var centers = new ArrayList<Vector2f>();
        for (ObstacleSegment segment : segments) {
            final boolean up = segment.vector().y() < 0, down = segment.vector().y() > 0;
            final byte code = segment.encoding();
            if (code == ARC_NW.$ && down) centers.add(segment.startPoint().toVector2f().plus(0, HTS));
            if (code == ARC_SW.$ && down) centers.add(segment.startPoint().toVector2f().plus(HTS, 0));
            if (code == ARC_SE.$ && up)   centers.add(segment.startPoint().toVector2f().plus(0, -HTS));
            if (code == ARC_NE.$ && up)   centers.add(segment.startPoint().toVector2f().plus(-HTS, 0));
        }
        return centers;
    }

    public List<Vector2i> computeInnerPolygon() {
        Vector2i start = startPoint;
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
                Vector2i e = segment.isNWCorner() || segment.isSECorner() ? vec2_int(0, v.y()) : vec2_int(v.x(), 0);
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