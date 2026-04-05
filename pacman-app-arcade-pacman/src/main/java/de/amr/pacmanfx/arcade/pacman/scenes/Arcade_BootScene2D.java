/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui.d2.GameScene2D;

/**
 * The boot screen is showing some strange screen patterns and eventually  a grid.
 * This scene tries to mimic that to a certain degree.
 */
public class Arcade_BootScene2D extends GameScene2D {

    public static final int BOOT_TIME_SECONDS = 4;

    public Arcade_BootScene2D() {}

    @Override
    public void update(Game game) {
        final State<Game> gameState = game.control().state();
        if (gameState.timer().atSecond(BOOT_TIME_SECONDS)) {
            gameState.expire();
        }
    }
}