/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.RectAreaFloat;
import de.amr.games.pacman.lib.Vector2f;

import java.util.*;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * Implements the Gourley/Green
 * <a href="https://ieeexplore.ieee.org/document/4037339">polygon-to-rectangle conversion algorithm</a>.
 */
public interface PolygonToRectConversion {

    static List<RectAreaFloat> convert(Obstacle obstacle) {
        Collection<Vector2f> innerPoints = computeInnerPoints(obstacle);
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

    static void flip(Collection<Vector2f> polygon, Vector2f p) {
        if (polygon.contains(p)) {
            polygon.remove(p);
        } else {
            polygon.add(p);
        }
    }

    static Collection<Vector2f> computeInnerPoints(Obstacle obstacle) {
        Vector2f startPoint = obstacle.startPoint().toVector2f();
        List<Vector2f> points = new ArrayList<>();
        points.add(startPoint);
        for (var segment : obstacle.segments()) {
            boolean down = segment.vector().y() > 0, up = segment.vector().y() < 0;
            float dx = 0, dy = 0;
            if (segment.isRoundedNWCorner()) {
                if (down) { dy = HTS; } else { dy = -HTS; }
            } else if (segment.isRoundedSWCorner()) {
                if (down) { dx = HTS; } else { dx = -HTS; }
            } else if (segment.isRoundedSECorner()) {
                if (up) { dy = -HTS;  } else { dy = HTS; }
            } else if (segment.isRoundedNECorner()) {
                if (up) { dx = -HTS; } else { dx = HTS; }
            }
            if (dx != 0 || dy != 0) {
                points.add(segment.startPoint().plus(dx, dy));
            }
            points.add(segment.endPoint().toVector2f());
        }

        // remove inverse pairs of edges and points inside edges
        List<Vector2f> edges = new ArrayList<>();
        for (int i = 0; i < points.size() - 1; ++i) {
            Vector2f edge = points.get(i + 1).minus(points.get(i));
            edges.add(edge);
        }

        Deque<Vector2f> stack = new ArrayDeque<>();
        for (Vector2f edge : edges) {
            if (stack.isEmpty()) {
                stack.push(edge);
            }
            else if (stack.peekFirst().equals(edge.inverse())) {
                stack.pop();
            } else {
                stack.push(edge);
            }
        }
        if (!stack.isEmpty() && stack.getFirst().equals(stack.getLast().inverse())) {
            startPoint = startPoint.plus(stack.getLast());
            stack.removeLast();
        }

        stack = stack.reversed();

        points.clear();
        Vector2f p = startPoint;
        points.add(p);
        Vector2f sumVector = null;
        for (Vector2f edge : stack) {
            if (sumVector == null) {
                sumVector = edge;
            }
            else if (sameDirection(sumVector, edge)) {
                sumVector = sumVector.plus(edge);
            } else {
                Vector2f q = p.plus(sumVector);
                points.add(q);
                sumVector = edge;
                p = q;
            }
        }
        return points;
    }

    static boolean sameDirection(Vector2f e, Vector2f f) {
        return Math.signum(e.x()) == Math.signum(f.x()) && Math.signum(e.y()) == Math.signum(f.y());
    }
}