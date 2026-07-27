/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.actors;

import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.systems.common.WorldMovementSystem;

public class GhostFactory {

    public static Ghost createRedGhostShadow(String name) {
        final Ghost ghost = new Ghost(GameModel.RED_GHOST_SHADOW, name);
        ghost.reset();
        return ghost;
    }

    public static Ghost createPinkGhostAmbusher(String name) {
        final Ghost ghost = new Ghost(GameModel.PINK_GHOST_SPEEDY, name);
        ghost.reset();
        return ghost;
    }

    public static Ghost createCyanGhostBashful(String name) {
        final Ghost ghost = new Ghost(GameModel.CYAN_GHOST_BASHFUL, name);
        ghost.reset();
        return ghost;
    }

    public static Ghost createOrangeGhostPokey(String name) {
        final Ghost ghost = new Ghost(GameModel.ORANGE_GHOST_POKEY, name);
        ghost.reset();
        return ghost;
    }
}
