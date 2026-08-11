/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gamestate.GameState_Booting;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;

/**
 * The boot screen displays some strange hex codes, garbage from the graphics memory
 * and eventually a grid (maybe used to calibrate the screen?). This scene tries to mimic that to a certain degree.
 */
public class Arcade_BootScene2D extends AbstractGameScene2D {

    public enum SceneState {
        BLANK,
        HEX_CODES,
        RANDOM_SPRITE_FRAGMENTS,
        GRID
    }

    public SceneState sceneState;

    public String[] noise = new String[28*36];

    public Arcade_BootScene2D(GameAppContext appContext) {
        super(appContext);

        // Make some noise
        final var rnd = new java.security.SecureRandom();
        for (int i = 0; i < noise.length; i++) {
            final byte hexDigit = (byte) rnd.nextInt(16);
            noise[i] = Integer.toHexString(hexDigit);
        }
    }

    @Override
    public void onActivate() {
        sceneState = SceneState.BLANK;
        gameContext().session().hud().hide();
    }

    @Override
    public void onTick(GameContext gameContext) {
        switch ((int) gameState().timer().tickCount()) {
            case GameState_Booting.Timing.HEX_CODES -> sceneState = SceneState.HEX_CODES;
            case GameState_Booting.Timing.SPRITE_GARBAGE -> sceneState = SceneState.RANDOM_SPRITE_FRAGMENTS;
            case GameState_Booting.Timing.GRID -> sceneState = SceneState.GRID;
        }
    }
}