/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.actors;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;

import static de.amr.pacmanfx.Globals.CYAN_GHOST_BASHFUL;
import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;

/**
 * The cyan ghost attacks Pac-Man from the opposite side of the red ghost.
 */
public class CyanGhostBashful extends Ghost {

    public CyanGhostBashful(String name) {
        super(CYAN_GHOST_BASHFUL, name);
        reset();
    }

    @Override
    public Vector2i chasingTargetTile(GameLevel gameLevel) {
        return gameLevel.pac().tilesAheadWithOverflowBug(2).scaled(2).minus(gameLevel.ghost(RED_GHOST_SHADOW).tile());
    }
}
