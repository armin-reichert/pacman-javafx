/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2i;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.vec_2i;

public interface ObstaclePartitioning {

    static List<RectArea> computeRectangularPartition(Obstacle obstacle) {
        List<Vector2i> innerPolygon = computeInnerPolygon(obstacle);
        List<RectArea> rectangles = new ArrayList<>();
        while (!innerPolygon.isEmpty()) {
            Vector2i p_k = minPoint(innerPolygon);
            Vector2i p_l = minPointExcluding(innerPolygon, p_k);
            Vector2i p_m = minPointBetween(innerPolygon, p_k, p_l);
            rectangles.add( spannedRectangle(p_k, vec_2i(p_l.x(), p_m.y())) );
            toggle(innerPolygon, p_k);
            toggle(innerPolygon, p_l);
            toggle(innerPolygon, vec_2i(p_k.x(), p_m.y()));
            toggle(innerPolygon, vec_2i(p_l.x(), p_m.y()));
        }
        return rectangles;
    }

    static RectArea spannedRectangle(Vector2i pMin, Vector2i pMax) {
        return new RectArea(pMin.x(), pMin.y(), pMax.x() - pMin.x(), pMax.y() - pMin.y());
    }

    static void toggle(List<Vector2i> polygon, Vector2i p) {
        if (polygon.contains(p)) polygon.remove(p); else polygon.add(p);
    }

    static Vector2i minPointBetween(List<Vector2i> polygon, Vector2i left, Vector2i right) {
        List<Vector2i> points = polygon.stream().filter(p -> left.x() <= p.x() && p.x() < right.x() && p.y() > left.y()).toList();
        return minPoint(points);
    }

    static Vector2i minPointExcluding(List<Vector2i> polygon, Vector2i excludedPoint) {
        return minPoint(polygon.stream().filter(p -> !p.equals(excludedPoint)).toList());
    }

    static Vector2i minPoint(List<Vector2i> polygon) {
        return polygon.stream().min(Comparator.comparingInt(Vector2i::x).thenComparingInt(Vector2i::y)).orElseThrow();
    }

    //TODO
    static List<Vector2i> computeInnerPolygon(Obstacle obstacle) {
//        return new ArrayList<>(
//            List.of(
//                vec_2i(8, 0), vec_2i(8, 24), vec_2i(0, 24), vec_2i(0, 32), vec_2i(8, 32), vec_2i(8, 48),
//                vec_2i(16, 48), vec_2i(16, 16), vec_2i(24, 16), vec_2i(24, 8), vec_2i(16, 8), vec_2i(16, 0)
//        ));

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
        if (polygon.getLast().equals(polygon.getFirst())) {
            polygon.removeLast();
        }
        return polygon;
    }
}