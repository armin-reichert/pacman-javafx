/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.actors;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;

import static de.amr.pacmanfx.Globals.CYAN_GHOST_BASHFUL;
import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;

public class Inky extends Ghost {

    protected Inky() {
        super(CYAN_GHOST_BASHFUL, "Inky");
        reset();
    }

    @Override
    public Vector2i chasingTargetTile(GameLevel gameLevel) {
        // Inky (cyan ghost) attacks from opposite side as Blinky
        return gameLevel.pac().tilesAheadWithOverflowBug(2).scaled(2).minus(gameLevel.ghost(RED_GHOST_SHADOW).tile());
    }
}
