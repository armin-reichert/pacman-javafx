/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.RectAreaFloat;
import de.amr.games.pacman.lib.Vector2f;

import java.util.*;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.vec_2f;

/**
 * Implements the Gourley/Green
 * <a href="https://ieeexplore.ieee.org/document/4037339">polygon-to-rectangle conversion algorithm</a>.
 */
public interface PolygonToRectConversion {

    static List<RectAreaFloat> convert(Obstacle obstacle) {
        Collection<Vector2f> innerPoints = computeInnerPolygonPoints(obstacle);
        List<RectAreaFloat> rectangles = new ArrayList<>();
        while (!innerPoints.isEmpty()) {
            Vector2f p_k = minPoint(obstacle, innerPoints.stream());
            Vector2f p_l = minPoint(obstacle, innerPoints.stream().filter(p -> !p.equals(p_k)));
            Vector2f p_m = minPoint(obstacle, innerPoints.stream().filter(p -> p_k.x() <= p.x() && p.x() < p_l.x() && p.y() > p_k.y()));
            var r = new RectAreaFloat(p_k.x(), p_k.y(), p_l.x() - p_k.x(), p_m.y() - p_k.y());
            rectangles.add(r);
            flip(innerPoints, p_k);
            flip(innerPoints, p_l);
            flip(innerPoints, vec_2f(p_k.x(), p_m.y()));
            flip(innerPoints, vec_2f(p_l.x(), p_m.y()));
        }
        return rectangles;
    }

    static Vector2f minPoint(Obstacle obstacle, Stream<Vector2f> points) {
        return points.min(Comparator.comparingDouble(Vector2f::y).thenComparingDouble(Vector2f::x))
            .orElseThrow(() -> new IllegalStateException("Obstacle with encoding '%s' caused error".formatted(obstacle.encoding())));
    }

    static void flip(Collection<Vector2f> points, Vector2f p) {
        if (points.contains(p)) { points.remove(p); } else { points.add(p); }
    }

    static Collection<Vector2f> computeInnerPolygonPoints(Obstacle obstacle) {
        Vector2f start = obstacle.startPoint().toVector2f();
        List<Vector2f> edges1 = replaceDiagonalCornerEdges(obstacle);
        List<Vector2f> edges2 = removeInversePairs(edges1);
        // Handle degenerate case
        if (edges2.size() > 2) {
            if (edges2.getLast().equals(edges2.getFirst().inverse())) {
                edges2.removeLast();
                edges2.removeLast();
                start = start.plus(edges2.removeFirst());
                start = start.plus(edges2.removeFirst());
            }
        }
        List<Vector2f> edges3 = compressEdges(edges2);
        return makeOpenPolygonPoints(start, edges3);
    }

    static List<Vector2f> replaceDiagonalCornerEdges(Obstacle obstacle) {
        List<Vector2f> edges = new ArrayList<>();
        for (var segment : obstacle.segments()) {
            boolean down = segment.vector().y() > 0, up = segment.vector().y() < 0;
            float dx = 0, dy = 0;
            if      (segment.isRoundedNWCorner()) { dy = down ? HTS : -HTS; }
            else if (segment.isRoundedSWCorner()) { dx = down ? HTS : -HTS; }
            else if (segment.isRoundedSECorner()) { dy = up ? -HTS : HTS; }
            else if (segment.isRoundedNECorner()) { dx = up ? -HTS : HTS; }
            if (dx != 0 || dy != 0) {
                Vector2f e1 = vec_2f(dx, dy), e2 = segment.vector().toVector2f().minus(e1);
                edges.add(e1);
                edges.add(e2);
            } else {
                edges.add(segment.vector().toVector2f());
            }
        }
        return edges;
    }

    static List<Vector2f> removeInversePairs(List<Vector2f> polygonEdges) {
        Deque<Vector2f> stack = new ArrayDeque<>();
        for (var edge : polygonEdges) {
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

    static List<Vector2f> compressEdges(List<Vector2f> polygonEdges) {
        List<Vector2f> edges = new ArrayList<>();
        if (polygonEdges.isEmpty()) {
            return edges;
        }
        edges.add(polygonEdges.getFirst());
        for (int i = 1; i < polygonEdges.size(); ++i) {
            Vector2f edge = polygonEdges.get(i);
            Vector2f last = edges.getLast();
            if (sameDirection(edge, last)) {
                edges.removeLast();
                edges.add(last.plus(edge));
            }
            else {
                edges.add(edge);
            }
        }
        return edges;
    }

    static List<Vector2f> makeOpenPolygonPoints(Vector2f startPoint, List<Vector2f> polygonEdges) {
        List<Vector2f> points = new ArrayList<>();
        points.add(startPoint);
        for (var edge : polygonEdges) {
            points.add(points.getLast().plus(edge));
        }
        if (points.getFirst().equals(points.getLast())) {
            points.removeLast();
        }
        return points;
    }

    static boolean sameDirection(Vector2f e, Vector2f f) {
        return Math.signum(e.x()) == Math.signum(f.x()) && Math.signum(e.y()) == Math.signum(f.y());
    }
}