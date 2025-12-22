/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengenmspacman.model.actors;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;

import static de.amr.pacmanfx.Globals.CYAN_GHOST_BASHFUL;
import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;

public class Inky extends Ghost {

    public Inky() {
        super(CYAN_GHOST_BASHFUL, "Inky");
        reset();
    }

    @Override
    public void onPacKilled(GameLevel gameLevel) {}

    @Override
    public Vector2i chasingTargetTile(GameLevel gameLevel) {
        Ghost blinky = gameLevel.ghost(RED_GHOST_SHADOW);
        return gameLevel.pac().tilesAhead(2).scaled(2).minus(blinky.tile());
    }
}
