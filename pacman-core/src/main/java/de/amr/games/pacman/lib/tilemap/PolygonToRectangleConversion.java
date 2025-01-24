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
 * Implements the polygon-to-rectangle conversion algorithm by Gourley/Green (1983).
 */
public interface PolygonToRectangleConversion {

    static List<RectArea> apply(Obstacle obstacle) {
        Logger.info(obstacle);
        Set<Vector2i> innerPoints = computeInnerPoints(obstacle);
        Logger.info("Inner points: {}", innerPoints);
        List<RectArea> rectangles = new ArrayList<>();
        while (!innerPoints.isEmpty()) {
            Vector2i p_k = minPoint(innerPoints.stream());
            Vector2i p_l = minPointExcluding(innerPoints, p_k);
            Vector2i p_m = minPointBetweenAndBelow(innerPoints, p_k, p_l);
            Vector2i p_max = vec_2i(p_l.x(), p_m.y());
            Logger.info("p_k={}   p_l={}   p_m={}   p_max={}", p_k, p_l, p_m, p_max);
            var r = new RectArea(p_k.x(), p_k.y(), p_max.x() - p_k.x(), p_max.y() - p_k.y());
            rectangles.add(r);
            Logger.info("rect={}", r);
            flip(innerPoints, p_k);
            flip(innerPoints, p_l);
            flip(innerPoints, vec_2i(p_k.x(), p_m.y()));
            flip(innerPoints, vec_2i(p_l.x(), p_m.y()));
            Logger.info("Inner polygon: {}", innerPoints);
        }
        return rectangles;
    }

    static void flip(Set<Vector2i> polygon, Vector2i p) {
        if (polygon.contains(p)) {
            polygon.remove(p);
            Logger.info("{} removed", p);
        } else {
            polygon.add(p);
            Logger.info("{} added", p);
        }
    }

    static Vector2i minPointBetweenAndBelow(Set<Vector2i> points, Vector2i left, Vector2i right) {
        return minPoint(points.stream().filter(p -> left.x() <= p.x() && p.x() < right.x() && p.y() > left.y()));
    }

    static Vector2i minPointExcluding(Set<Vector2i> polygon, Vector2i excludedPoint) {
        return minPoint(polygon.stream().filter(p -> !p.equals(excludedPoint)));
    }

    static Vector2i minPoint(Stream<Vector2i> points) {
        return points.min(Comparator.comparingInt(Vector2i::y).thenComparingInt(Vector2i::x)).orElseThrow();
    }

    static Set<Vector2i> computeInnerPoints(Obstacle obstacle) {
        List<Vector2i> polygon = new ArrayList<>();
        polygon.add(obstacle.startPoint());
        for (var segment : obstacle.segments()) {
            boolean down = segment.vector().y() > 0, up = !down;
            switch (segment.encoding()) {
                case TileEncoding.CORNER_NW -> {
                    Vector2i p = down ? segment.startPoint().plus(0, HTS) : segment.startPoint().plus(0, -HTS);
                    polygon.add(p);
                    polygon.add(segment.endPoint());
                }
                case TileEncoding.CORNER_SW -> {
                    Vector2i p = down ? segment.startPoint().plus(HTS, 0) : segment.startPoint().plus(-HTS, 0);
                    polygon.add(p);
                    polygon.add(segment.endPoint());
                }
                case TileEncoding.CORNER_SE -> {
                    Vector2i p = up ? segment.startPoint().plus(0, -HTS) : segment.startPoint().plus(0, HTS);
                    polygon.add(p);
                    polygon.add(segment.endPoint());
                }
                case TileEncoding.CORNER_NE -> {
                    Vector2i p = up ? segment.startPoint().plus(-HTS, 0) : segment.startPoint().plus(HTS, 0);
                    polygon.add(p);
                    polygon.add(segment.endPoint());
                }
                default -> polygon.add(segment.endPoint());
            }
        }
        //TODO remove points inside edges
        List<Vector2i> edges = new ArrayList<>();
        for (int i = 0; i < polygon.size() - 1; ++i) {
            Vector2i edge = polygon.get(i + 1).minus(polygon.get(i));
            edges.add(edge);
        }
        polygon.clear();
        Vector2i p = obstacle.startPoint();
        polygon.add(p);
        Vector2i v = null;
        for (Vector2i edge : edges) {
            if (v == null) {
                v = edge;
            }
            else if (sameDirection(v, edge)) {
                v = v.plus(edge);
            } else {
                Vector2i q = p.plus(v);
                polygon.add(q);
                v = edge;
                p = q;
            }
        }

        return new HashSet<>(polygon);
    }

    static boolean sameDirection(Vector2i e, Vector2i f) {
        return e.x() == 0 && f.x() == 0 || e.y() == 0 && f.y() == 0;
    }
}