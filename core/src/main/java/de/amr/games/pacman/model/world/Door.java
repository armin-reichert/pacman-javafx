/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.world;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * @author Armin Reichert
 */
public record Door(Vector2i leftWing, Vector2i rightWing) {

    public Door {
        checkNotNull(leftWing);
        checkNotNull(rightWing);
    }

    /**
     * @param tile some tile
     * @return tells if the given tile is occupied by this door
     */
    public boolean occupies(Vector2i tile) {
        return leftWing.equals(tile) || rightWing.equals(tile);
    }

    /**
     * @return position where ghost can enter the door
     */
    public Vector2f entryPosition() {
        return v2f(TS * rightWing.x() - HTS, TS * (rightWing.y() - 1));
    }
}