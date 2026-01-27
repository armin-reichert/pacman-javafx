/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui._2d.GameScene2D;

/**
 * The boot screen is showing some strange screen patterns and eventually  a grid.
 * This scene tries to mimic that to a certain degree.
 */
public class Arcade_BootScene2D extends GameScene2D {

    public Arcade_BootScene2D() {}

    @Override
    public void doInit(Game game) {
        game.hud().hide();
   }

    @Override
    protected void doEnd(Game game) {}

    @Override
    public void update(Game game) {
        if (game.control().state().timer().atSecond(4)) {
            game.control().terminateCurrentGameState();
        }
    }
}