/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2i;

import java.util.*;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.vec_2i;

/**
 * Implements the Gourley/Green
 * <a href="https://github.com/armin-reichert/pacman-javafx/blob/main/doc/gourley1983.pdf">polygon-to-rectangle conversion algorithm</a>.
 */
public interface PolygonToRectConversion {

    static List<RectArea> convert(Obstacle obstacle) {
        List<RectArea> rectangles = new ArrayList<>();
        Collection<Vector2i> points = computeInnerPolygonPoints(obstacle);
        while (!points.isEmpty()) {
            Vector2i p_k = minPoint(obstacle, points.stream());
            Vector2i p_l = minPoint(obstacle, points.stream().filter(p -> !p.equals(p_k)));
            Vector2i p_m = minPoint(obstacle, points.stream().filter(p -> p_k.x() <= p.x() && p.x() < p_l.x() && p.y() > p_k.y()));
            Vector2i p_km = new Vector2i(p_k.x(), p_m.y());
            Vector2i p_lm = new Vector2i(p_l.x(), p_m.y());

            var r = new RectArea(p_k.x(), p_k.y(), p_l.x() - p_k.x(), p_m.y() - p_k.y());
            rectangles.add(r);

            points.remove(p_k);
            points.remove(p_l);
            if (points.contains(p_km)) points.remove(p_km); else points.add(p_km);
            if (points.contains(p_lm)) points.remove(p_lm); else points.add(p_lm);
        }
        return rectangles;
    }

    static Vector2i minPoint(Obstacle obstacle, Stream<Vector2i> points) {
        return points.min(Comparator.comparingDouble(Vector2i::y).thenComparingDouble(Vector2i::x))
            .orElseThrow(() -> new IllegalStateException("Obstacle with encoding '%s' caused error".formatted(obstacle.encoding())));
    }

    static Collection<Vector2i> computeInnerPolygonPoints(Obstacle obstacle) {
        Vector2i start = obstacle.startPoint();
        List<Vector2i> edges1 = replaceDiagonalCornerEdges(obstacle);
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

    static List<Vector2i> replaceDiagonalCornerEdges(Obstacle obstacle) {
        List<Vector2i> edges = new ArrayList<>();
        for (var segment : obstacle.segments()) {
            boolean down = segment.vector().y() > 0, up = segment.vector().y() < 0;
            int dx = 0, dy = 0;
            if      (segment.isRoundedNWCorner()) { dy = down ? HTS : -HTS; }
            else if (segment.isRoundedSWCorner()) { dx = down ? HTS : -HTS; }
            else if (segment.isRoundedSECorner()) { dy = up ? -HTS : HTS; }
            else if (segment.isRoundedNECorner()) { dx = up ? -HTS : HTS; }
            if (dx != 0 || dy != 0) {
                Vector2i e1 = vec_2i(dx, dy), e2 = segment.vector().minus(e1);
                edges.add(e1);
                edges.add(e2);
            } else {
                edges.add(segment.vector());
            }
        }
        return edges;
    }

    static List<Vector2i> removeInversePairs(List<Vector2i> polygonEdges) {
        Deque<Vector2i> stack = new ArrayDeque<>();
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

    static List<Vector2i> combineEdgesWithSameDirection(List<Vector2i> polygonEdges) {
        List<Vector2i> edges = new ArrayList<>();
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
            }
            else {
                edges.add(edge);
            }
        }
        return edges;
    }

    static List<Vector2i> makeOpenPolygonPoints(Vector2i startPoint, List<Vector2i> polygonEdges) {
        List<Vector2i> points = new ArrayList<>();
        points.add(startPoint);
        for (var edge : polygonEdges) {
            points.add(points.getLast().plus(edge));
        }
        if (points.getFirst().equals(points.getLast())) {
            points.removeLast();
        }
        return points;
    }

    static boolean sameDirection(Vector2i e, Vector2i f) {
        return Math.signum(e.x()) == Math.signum(f.x()) && Math.signum(e.y()) == Math.signum(f.y());
    }
}