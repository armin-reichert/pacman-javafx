/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.gamestate.CommonGameBootState;
import de.amr.pacmanfx.ui.game.Game;
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

    public Arcade_BootScene2D(Game game) {
        super(game);

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
    }

    @Override
    public void onTick(long tick) {
        switch ((int) gameState().timer().tickCount()) {
            case CommonGameBootState.Timing.HEX_CODES -> sceneState = SceneState.HEX_CODES;
            case CommonGameBootState.Timing.SPRITE_GARBAGE -> sceneState = SceneState.RANDOM_SPRITE_FRAGMENTS;
            case CommonGameBootState.Timing.GRID -> sceneState = SceneState.GRID;
        }
    }
}