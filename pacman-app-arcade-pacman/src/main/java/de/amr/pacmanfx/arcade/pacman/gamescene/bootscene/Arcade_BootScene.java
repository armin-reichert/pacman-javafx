/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamescene.bootscene;

import de.amr.basics.timer.TickTimer;
import de.amr.basics.ui.entities.hud.HUDStyleComp;
import de.amr.basics.ui.entities.props.CanvasClear;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;

import java.util.stream.Stream;

/**
 * The boot screen displays some strange hex codes, garbage from the graphics memory
 * and eventually a grid (maybe used to calibrate the screen?). This scene tries to mimic that to a certain degree.
 */
public class Arcade_BootScene extends AbstractGameScene {

    public static final int GRID_SIZE = 16;
    public static final int WIDTH_IN_TILES  = 28;
    public static final int HEIGHT_IN_TILES = 36;

    private static final CanvasClear CANVAS_CLEAR = new CanvasClear();
    private static final Renderable GRID = new GridPattern(GRID_SIZE, WIDTH_IN_TILES, HEIGHT_IN_TILES);

    public enum SceneState {
        DARK(0),
        HEX_CODES(60),
        SPRITE_NOISE(120),
        GRID(210),
        ANIMATION_COMPLETE(240);

        SceneState(int startTick) {
            this.startTick = startTick;
        }

        public int startTick() {
            return startTick;
        }

        private final int startTick;
    }

    public SceneState currentState;

    private Renderable currentSceneContent;

    public Arcade_BootScene() {
        view2D().setAutoClearCanvas(false);
    }

    @Override
    public Stream<Renderable> renderables() {
        return Ufx.streamOf(currentSceneContent);
    }

    @Override
    public void onActivate() {
        currentState = SceneState.DARK;

        game().session().setHudVisible(false);
        //TODO This is only a temporary solution
        setHUDStyle(game().session().hud());
    }

    @Override
    public void onTick(GameContext game) {
        final TickTimer timer = game.state().timer();

        if (timer.hasExpired()) {
            return;
        }

        if (currentState == SceneState.ANIMATION_COMPLETE) {
            timer.expire();
            return;
        }

        final long t = timer.tickCount();

        // Start next state?
        for (var nextState : SceneState.values()) {
            if (t == nextState.startTick()) {
                currentState = nextState;
            }
        }

        final int mod4 = (int) (t - currentState.startTick()) % 4;

        switch (currentState) {
            case DARK -> currentSceneContent = CANVAS_CLEAR;

            case HEX_CODES -> {
                if (mod4 == 0) {
                    currentSceneContent = CANVAS_CLEAR;
                } else if (mod4 == 1) {
                    currentSceneContent = HexDigitsBlock.randomHexDigits(WIDTH_IN_TILES, HEIGHT_IN_TILES);
                }
            }

            case SPRITE_NOISE -> {
                if (mod4 == 0) {
                    currentSceneContent = CANVAS_CLEAR;
                } else if (mod4 == 1) {
                    currentSceneContent = SpritesBlock.randomSpritesBlock(16, WIDTH_IN_TILES, HEIGHT_IN_TILES);
                }
            }

            case GRID -> {
                if (t == currentState.startTick()) {
                    currentSceneContent = CANVAS_CLEAR;
                } else {
                    currentSceneContent = GRID;
                }
            }
        }
    }

    private void setHUDStyle(HUD hud) {
        final HUDStyleComp hudStyle = app().gameVariantManager().currentRuntime().uiConfig().renderConfig().hudStyle();
        hud.levelCounter().setComponent(HUDStyleComp.class, hudStyle);
        hud.livesCounter().setComponent(HUDStyleComp.class, hudStyle);
        hud.gameScore().setComponent(HUDStyleComp.class, hudStyle);
        hud.highScore().setComponent(HUDStyleComp.class, hudStyle);
        hud.creditDisplay().setComponent(HUDStyleComp.class, hudStyle);
    }
}
