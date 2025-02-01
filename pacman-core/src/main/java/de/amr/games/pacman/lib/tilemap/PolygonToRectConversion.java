/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2i;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Comparator.comparingDouble;

/**
 * Implements the Gourley/Green
 * <a href="https://github.com/armin-reichert/pacman-javafx/blob/main/doc/gourley1983.pdf">polygon-to-rectangle conversion algorithm</a>.
 */
public interface PolygonToRectConversion {

    static List<RectArea> convert(Obstacle obstacle) {
        var rectangles = new ArrayList<RectArea>();
        Collection<Vector2i> points = obstacle.computeInnerPolygonPoints();
        while (!points.isEmpty()) {
            Vector2i p_k = minPoint(points.stream());
            Vector2i p_l = minPoint(points.stream().filter(p -> !p.equals(p_k)));
            // Note: In the original paper, the condition is p.x() < p_l.x() but that leads to incorrect answers for some polygons!
            // After changing the condition to p.x() <= p_l.x(), the problem disappeared!
            Vector2i p_m = minPoint(points.stream().filter(p -> p_k.x() <= p.x() && p.x() <= p_l.x() && p.y() > p_k.y()));
            Vector2i p_km = new Vector2i(p_k.x(), p_m.y());
            Vector2i p_lm = new Vector2i(p_l.x(), p_m.y());

            var r = new RectArea(p_k.x(), p_k.y(), p_l.x() - p_k.x(), p_m.y() - p_k.y());
            rectangles.add(r);

            points.remove(p_k);
            points.remove(p_l);
            if (points.contains(p_km)) points.remove(p_km); else points.add(p_km);
            if (points.contains(p_lm)) points.remove(p_lm); else points.add(p_lm);
        }
        Logger.debug("Inner obstacle area rectangles computed");
        return rectangles;
    }

    static Vector2i minPoint(Stream<Vector2i> points) {
        return points.min(comparingDouble(Vector2i::y).thenComparingDouble(Vector2i::x))
            .orElseThrow(() -> new IllegalStateException("Error converting polygon to rectangles " + points));
    }
}