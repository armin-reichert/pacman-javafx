/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2i;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.vec_2i;

/**
 * Implements the Gourley/Green
 * <a href="https://ieeexplore.ieee.org/document/4037339">polygon-to-rectangle conversion algorithm</a>.
 */
public interface PolygonToRectSet {

    static List<RectArea> apply(Obstacle obstacle) {
        Logger.info(obstacle);
        Collection<Vector2i> innerPoints = computeInnerPoints(obstacle);
        List<RectArea> rectangles = new ArrayList<>();
        while (!innerPoints.isEmpty()) {
            Logger.info("Inner points: {}", innerPoints);
            Vector2i p_k = minPoint(innerPoints.stream());
            Vector2i p_l = minPoint(innerPoints.stream().filter(p -> !p.equals(p_k)));
            Vector2i p_m = minPoint(innerPoints.stream().filter(p -> p_k.x() <= p.x() && p.x() < p_l.x() && p.y() > p_k.y()));
            var r = new RectArea(p_k.x(), p_k.y(), p_l.x() - p_k.x(), p_m.y() - p_k.y());
            Logger.info("p_k={}   p_l={}   p_m={}", p_k, p_l, p_m);
            Logger.info(r);
            rectangles.add(r);
            flip(innerPoints, p_k);
            flip(innerPoints, p_l);
            flip(innerPoints, vec_2i(p_k.x(), p_m.y()));
            flip(innerPoints, vec_2i(p_l.x(), p_m.y()));
        }
        return rectangles;
    }

    static Vector2i minPoint(Stream<Vector2i> points) {
        return points.min(Comparator.comparingInt(Vector2i::y).thenComparingInt(Vector2i::x)).orElseThrow();
    }

    static void flip(Collection<Vector2i> polygon, Vector2i p) {
        if (polygon.contains(p)) {
            polygon.remove(p);
            Logger.info("{} removed", p);
        } else {
            polygon.add(p);
            Logger.info("{} added", p);
        }
    }

    static Collection<Vector2i> computeInnerPoints(Obstacle obstacle) {
        List<Vector2i> points = new ArrayList<>();
        points.add(obstacle.startPoint());
        for (var segment : obstacle.segments()) {
            boolean down = segment.vector().y() > 0, up = segment.vector().y() < 0;
            int dx = 0, dy = 0;
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
            points.add(segment.endPoint());
        }

        // remove inverse pairs of edges and points inside edges
        List<Vector2i> edges = new ArrayList<>();
        for (int i = 0; i < points.size() - 1; ++i) {
            Vector2i edge = points.get(i + 1).minus(points.get(i));
            edges.add(edge);
        }

        Deque<Vector2i> stack = new ArrayDeque<>();
        for (Vector2i edge : edges) {
            if (stack.isEmpty()) {
                stack.push(edge);
            }
            else if (stack.peekFirst().plus(edge).equals(Vector2i.ZERO)) {
                stack.pop();
            } else {
                stack.push(edge);
            }
        }
        stack = stack.reversed();

        points.clear();
        Vector2i p = obstacle.startPoint();
        points.add(p);
        Vector2i sumVector = null;
        for (Vector2i edge : stack) {
            if (sumVector == null) {
                sumVector = edge;
            }
            else if (sameDirection(sumVector, edge)) {
                sumVector = sumVector.plus(edge);
            } else {
                Vector2i q = p.plus(sumVector);
                points.add(q);
                sumVector = edge;
                p = q;
            }
        }
        return points;
    }

    static boolean sameDirection(Vector2i e, Vector2i f) {
        return Math.signum(e.x()) == Math.signum(f.x()) && Math.signum(e.y()) == Math.signum(f.y());
    }
}