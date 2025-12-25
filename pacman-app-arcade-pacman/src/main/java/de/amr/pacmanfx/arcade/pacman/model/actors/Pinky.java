/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.model.actors;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;

import static de.amr.pacmanfx.Globals.PINK_GHOST_SPEEDY;

public class Pinky extends Ghost {

    public Pinky() {
        super(PINK_GHOST_SPEEDY, "Pinky");
        reset();
    }

    @Override
    public void onPacKilled(GameLevel gameLevel) {}

    @Override
    public Vector2i chasingTargetTile(GameLevel gameLevel) {
        // Pinky (pink ghost) ambushes Pac-Man
        return gameLevel.pac().tilesAheadWithOverflowBug(4);
    }
}
