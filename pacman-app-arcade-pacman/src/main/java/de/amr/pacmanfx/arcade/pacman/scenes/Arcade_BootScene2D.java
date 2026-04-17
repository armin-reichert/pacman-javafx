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

    private SceneState sceneState;

    public SceneState state() {
        return sceneState;
    }

    @Override
    public void onSceneStart() {
        sceneState = SceneState.BLANK;
    }

    @Override
    public void onTick(long tick) {
        final State<Game> gameState = gameContext().game().flow().state();
        final long stateTick = gameState.timer().tickCount();
        switch ((int) stateTick) {
            case  60 -> sceneState = SceneState.HEX_CODES;
            case 120 -> sceneState = SceneState.RANDOM_SPRITE_FRAGMENTS;
            case 210 -> sceneState = SceneState.GRID;
            case 240 -> gameState.expire();
        }
    }
}