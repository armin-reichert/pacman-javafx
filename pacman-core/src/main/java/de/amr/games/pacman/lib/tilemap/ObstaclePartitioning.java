/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2i;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public interface ObstaclePartitioning {

    static List<RectArea> computePartition(Obstacle obstacle) {
        List<RectArea> rectangleList = new ArrayList<>();
        List<Vector2i> polygon = computeInnerPolygon(obstacle);
        while (!polygon.isEmpty()) {
            Vector2i p_k = min_point(polygon);
            Vector2i p_l = min_point_except(polygon, p_k);
            Vector2i p_m = min_point_between(polygon, p_k, p_l);
            rectangleList.add( span(p_k, new Vector2i(p_l.x(), p_m.y())) );
            toggle(polygon, p_k);
            toggle(polygon, p_l);
            toggle(polygon, new Vector2i(p_k.x(), p_m.y()));
            toggle(polygon, new Vector2i(p_l.x(), p_m.y()));
        }
        return rectangleList;
    }

    static RectArea span(Vector2i p_min, Vector2i p_max) {
        return new RectArea(p_min.x(), p_min.y(), p_max.x() - p_min.x(), p_max.y() - p_min.y());
    }

    static void toggle(List<Vector2i> polygon, Vector2i p) {
        if (polygon.contains(p)) {
            polygon.remove(p);
        } else {
            polygon.add(p);
        }
    }

    static Vector2i min_point_between(List<Vector2i> polygon, Vector2i left, Vector2i right) {
        List<Vector2i> points = polygon.stream().filter(p -> left.x() <= p.x() && p.x() < right.x() && p.y() > left.y()).toList();
        return min_point(points);
    }

    static Vector2i min_point_except(List<Vector2i> polygon, Vector2i p) {
        polygon.remove(p);
        Vector2i min = min_point(polygon);
        polygon.add(p);
        return min;
    }

    static Vector2i min_point(List<Vector2i> polygon) {
        return polygon.stream().min(Comparator.comparingInt(Vector2i::y).thenComparingInt(Vector2i::x)).orElseThrow();
    }

    static List<Vector2i> computeInnerPolygon(Obstacle obstacle) {
        return List.of(); //TODO
    }
}
