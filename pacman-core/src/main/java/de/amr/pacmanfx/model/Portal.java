/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.Vector2i;

/**
 * A portal connects two border tiles on the left and right map border. Traveling through the portal corresponds to
 * moving over <code>2 * portalDepth</code> tiles.
 */
public record Portal(Vector2i leftBorderPortalEntry, Vector2i rightBorderPortalEntry, int depth) {

    public boolean contains(Vector2i tile) {
        for (int numTiles = 1; numTiles <= depth; ++numTiles) {
            Vector2i leftPortalTile = leftBorderPortalEntry.minus(numTiles, 0);
            Vector2i rightPortalTile = rightBorderPortalEntry.plus(numTiles, 0);
            if (tile.equals(leftPortalTile) || tile.equals(rightPortalTile)) {
                return true;
            }
        }
        return false;
    }
}