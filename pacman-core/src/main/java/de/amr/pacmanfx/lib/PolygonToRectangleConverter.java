/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib;

import de.amr.pacmanfx.lib.math.Vector2i;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Comparator.comparingDouble;

/**
 * Implements the Gourley/Green
 * <a href="https://github.com/armin-reichert/pacman-javafx/blob/main/doc/gourley1983.pdf">polygon-to-rectangle conversion algorithm</a>.
 *
 * @param <RECTANGLE_TYPE> type of rectangles returned by the algorithm
 */
public interface PolygonToRectangleConverter<RECTANGLE_TYPE> {

    private static Vector2i minPoint(Stream<Vector2i> points) {
        return points.min(comparingDouble(Vector2i::y).thenComparingDouble(Vector2i::x)).orElseThrow();
    }

    /**
     * @param x      left-upper corner x
     * @param y      left-upper-corner y
     * @param width  width of rectangle
     * @param height height of rectangle
     * @return rectangle with given top-left position and size
     */
    RECTANGLE_TYPE createRectangle(int x, int y, int width, int height);

    // Note: In the original paper, the condition for p_m is p.x() < p_l.x() but that leads to incorrect results for some polygons!
    // After changing the condition to p.x() <= p_l.x(), the problem disappeared!
    default List<RECTANGLE_TYPE> convertPolygonToRectangles(Collection<Vector2i> points) {
        if (points == null) {
            throw new IllegalArgumentException("Points array must not be NULL");
        }
        if (points.isEmpty()) {
            return List.of();
        }
        var rectangles = new ArrayList<RECTANGLE_TYPE>();
        while (!points.isEmpty()) {
            Vector2i pk = minPoint(points.stream());
            Vector2i pl = minPoint(points.stream().filter(p -> !p.equals(pk)));
            Vector2i pm = minPoint(points.stream().filter(p -> pk.x() <= p.x() && p.x() <= pl.x() && p.y() > pk.y()));
            Vector2i pkm = new Vector2i(pk.x(), pm.y());
            Vector2i plm = new Vector2i(pl.x(), pm.y());

            var rectangle = createRectangle(pk.x(), pk.y(), pl.x() - pk.x(), pm.y() - pk.y());
            if (rectangle == null) {
                throw new IllegalArgumentException("createRectangle() method returned NULL");
            }
            rectangles.add(rectangle);

            points.remove(pk);
            points.remove(pl);
            if (points.contains(pkm)) points.remove(pkm); else points.add(pkm);
            if (points.contains(plm)) points.remove(plm); else points.add(plm);
        }
        return rectangles;
    }
}