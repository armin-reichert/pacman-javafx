/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.fsm.State;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.ui.app.AppContext;
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

    public SceneState sceneState;

    public Arcade_BootScene2D(AppContext context) {
        super(context);
    }

    @Override
    public void onActivate(AppContext context) {
        sceneState = SceneState.BLANK;
    }

    @Override
    public void onTick(long tick) {
        final State<GameContext> bootState = appContext().currentGameContext().state();
        switch ((int) bootState.timer().tickCount()) {
            case  60 -> sceneState = SceneState.HEX_CODES;
            case 120 -> sceneState = SceneState.RANDOM_SPRITE_FRAGMENTS;
            case 210 -> sceneState = SceneState.GRID;
            case 240 -> bootState.expire();
        }
    }
}