/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.model.actors;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;

import static de.amr.pacmanfx.Globals.ORANGE_GHOST_POKEY;

/**
 * The orange ghost attacks Pac-Man directly or retreats towards scatter target if Pac is nearby.
 */
public class OrangeGhostPokey extends Ghost {

    public static final int ESCAPE_DISTANCE_IN_TILES = 8;

    public OrangeGhostPokey(String name) {
        super(ORANGE_GHOST_POKEY, name);
        reset();
    }

    @Override
    public Vector2i chasingTargetTile(GameLevel gameLevel) {
        return tile().euclideanDist(gameLevel.pac().tile()) < ESCAPE_DISTANCE_IN_TILES
            ? gameLevel.worldMap().terrainLayer().ghostScatterTile(personality())
            : gameLevel.pac().tile();
    }
}
