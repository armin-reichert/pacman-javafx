/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.ecs.systems;

import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import javafx.geometry.Rectangle2D;

import static java.util.Objects.requireNonNull;

public class PositionSystem {

    public static final int TILE_SIZE = 8;
    public static final int HALF_TILE_SIZE = 4;

    /**
     * @param p a point in the plane
     * @return the "tile" containing the given point
     */
    public static Vector2i computeTileAt(Vector2f p) {
        requireNonNull(p);

        final float tx = p.x() >= 0 ? p.x() / TILE_SIZE : (p.x() - TILE_SIZE) / TILE_SIZE;
        final float ty = p.y() >= 0 ? p.y() / TILE_SIZE : (p.y() - TILE_SIZE) / TILE_SIZE;
        return new Vector2i((int) tx, (int) ty);
    }

    /**
     * @return offset of actor position relative to current tile: (0, 0) if centered, range: [-4, +4)
     */
    public static Vector2i computeTileOffset(Vector2f p) {
        requireNonNull(p);

        final Vector2i tile = computeTileAt(p);
        int ox = (int)p.x() - tile.x() * TILE_SIZE;
        int oy = (int)p.y() - tile.y() * TILE_SIZE;
        return new Vector2i(
            ox >= HALF_TILE_SIZE ? ox - TILE_SIZE : ox,
            oy >= HALF_TILE_SIZE ? oy - TILE_SIZE : oy);
    }

    /**
     * @param p point in the plane (left upper corner of bounding box of size one square tile)
     * @return the bounding box, a rectangle of size 1/2 tile with left upper corner at the given position
     */
    public static Rectangle2D boundingBox(Vector2f p) {
        requireNonNull(p);

        return new Rectangle2D(p.x(), p.y(), TILE_SIZE, TILE_SIZE);
    }

    public static Vector2f boundingBoxCenter(Vector2f p) {
        requireNonNull(p);

        return new Vector2f(p.x() + HALF_TILE_SIZE, p.y() + HALF_TILE_SIZE);
    }
}
