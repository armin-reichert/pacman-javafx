/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.math.RandomNumbers;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.SceneCanvasRenderingComp;

import java.util.EnumMap;
import java.util.Map;

/**
 * The boot screen displays some strange hex codes, garbage from the graphics memory
 * and eventually a grid (maybe used to calibrate the screen?). This scene tries to mimic that to a certain degree.
 */
public class Arcade_BootScene2D extends GameScene {

    public enum SceneState {
        BLANK,
        HEX_CODES,
        RANDOM_SPRITE_FRAGMENTS,
        GRID,
        EXPIRATION
    }

    public static final Map<SceneState, Integer> TICKS = new EnumMap<>(Map.of(
        SceneState.BLANK, 0,
        SceneState.HEX_CODES, 60,
        SceneState.RANDOM_SPRITE_FRAGMENTS, 120,
        SceneState.GRID, 210,
        SceneState.EXPIRATION, 240
    ));

    public SceneState sceneState;

    public String[] noise = new String[28*36];

    public Arcade_BootScene2D(GameAppContext app) {
        super(app);

        final var canvasRendering = new SceneCanvasRenderingComp();
        canvasRendering.setClearCanvasBeforeRendering(false);
        setComp(SceneCanvasRenderingComp.class, canvasRendering);

        // Make some noise
        for (int i = 0; i < noise.length; i++) {
            final byte hexDigit = (byte) RandomNumbers.randomInt(0, 16);
            noise[i] = Integer.toHexString(hexDigit);
        }
    }

    @Override
    public void onActivate() {
        sceneState = SceneState.BLANK;
        game().session().hud().hide();
    }

    @Override
    public void onTick(GameContext game) {
        final long tick = game().state().timer().tickCount();
        if (tick == TICKS.get(SceneState.HEX_CODES)) {
            sceneState = SceneState.HEX_CODES;
        }
        else if (tick == TICKS.get(SceneState.RANDOM_SPRITE_FRAGMENTS)) {
            sceneState = SceneState.RANDOM_SPRITE_FRAGMENTS;
        }
        else if (tick == TICKS.get(SceneState.GRID)) {
            sceneState = SceneState.GRID;
        }
        else if (tick == TICKS.get(SceneState.EXPIRATION)) {
            game().state().timer().expire();
        }
    }
}