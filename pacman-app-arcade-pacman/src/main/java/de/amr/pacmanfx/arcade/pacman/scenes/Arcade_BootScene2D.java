/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui.d2.GameScene2D;

/**
 * The boot screen displays some strange hex codes, garbage from the graphics memory
 * and eventually a grid (maybe used to calibrate the screen?). This scene tries to mimic that to a certain degree.
 */
public class Arcade_BootScene2D extends GameScene2D {

    public enum SceneState {
        BLANK,
        HEX_CODES,
        RANDOM_SPRITE_FRAGMENTS,
        GRID
    }

    private SceneState state;

    public SceneState state() {
        return state;
    }

    @Override
    protected void doInit(Game game) {
        state = SceneState.BLANK;
    }

    @Override
    public void update(Game game) {
        final State<Game> gameState = game.flow().state();
        final long tick = gameState.timer().tickCount();
        if (tick == 60 )      { state = SceneState.HEX_CODES; }
        else if (tick == 120) { state = SceneState.RANDOM_SPRITE_FRAGMENTS; }
        else if (tick == 210) { state = SceneState.GRID; }
        else if (tick == 240) { gameState.expire(); }
    }
}