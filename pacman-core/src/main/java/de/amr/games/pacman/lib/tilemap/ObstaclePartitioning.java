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

import static de.amr.games.pacman.lib.Globals.vec_2i;

public interface ObstaclePartitioning {

    static List<RectArea> computeRectangularPartition(Obstacle obstacle) {
        List<Vector2i> polygon = computeInnerPolygon(obstacle);
        List<RectArea> rectangles = new ArrayList<>();
        while (!polygon.isEmpty()) {
            Vector2i p_k = minPoint(polygon.stream()),
                    p_l = minPointExcluding(polygon.stream(), p_k),
                    p_m = minPointBetween(polygon.stream(), p_k, p_l);
            rectangles.add( spannedRectangle(p_k, vec_2i(p_l.x(), p_m.y())) );
            toggle(polygon, p_k);
            toggle(polygon, p_l);
            toggle(polygon, vec_2i(p_k.x(), p_m.y()));
            toggle(polygon, vec_2i(p_l.x(), p_m.y()));
        }
        return rectangles;
    }

    static RectArea spannedRectangle(Vector2i pMin, Vector2i pMax) {
        return new RectArea(pMin.x(), pMin.y(), pMax.x() - pMin.x(), pMax.y() - pMin.y());
    }

    static void toggle(List<Vector2i> polygon, Vector2i p) {
        if (polygon.contains(p)) polygon.remove(p); else polygon.add(p);
    }

    static Vector2i minPointBetween(Stream<Vector2i> polygon, Vector2i left, Vector2i right) {
        Stream<Vector2i> points = polygon.filter(p -> left.x() <= p.x() && p.x() < right.x() && p.y() > left.y());
        return minPoint(points);
    }

    static Vector2i minPointExcluding(Stream<Vector2i> polygon, Vector2i excludedPoint) {
        return minPoint(polygon.filter(p -> p != excludedPoint));
    }

    static Vector2i minPoint(Stream<Vector2i> polygon) {
        return polygon.min(Comparator.comparingInt(Vector2i::y).thenComparingInt(Vector2i::x)).orElseThrow();
    }

    //TODO
    static List<Vector2i> computeInnerPolygon(Obstacle obstacle) {
        return new ArrayList<>(
            List.of(
                vec_2i(8, 0), vec_2i(8, 24), vec_2i(0, 24), vec_2i(0, 32), vec_2i(8, 32), vec_2i(8, 48),
                vec_2i(16, 48), vec_2i(16, 16), vec_2i(24, 16), vec_2i(24, 8), vec_2i(16, 8), vec_2i(16, 0)
        ));
        //Vector2i p = obstacle.startPoint();
        //return List.of();
    }

    public static void main(String[] args) {
        List<RectArea> rects = computeRectangularPartition(null);
        for (var r : rects) {
            Logger.info(r);
        }
    }
}