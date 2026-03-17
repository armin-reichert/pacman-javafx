/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.model.actors;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;

import static de.amr.pacmanfx.Globals.PINK_GHOST_SPEEDY;

/**
 * The pink ghost ambushes Pac-Man.
 */
public class PinkGhostAmbusher extends Ghost {

    public PinkGhostAmbusher(String name) {
        super(PINK_GHOST_SPEEDY, name);
        reset();
    }

    @Override
    public Vector2i chasingTargetTile(GameLevel gameLevel) {
        return gameLevel.pac().tilesAheadWithOverflowBug(4);
    }
}
