/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes.bootscene;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.SceneCanvasRenderingComp;
import de.amr.pacmanfx.uilib.entities.hud.comp.HUD_Style;

import java.util.stream.Stream;

/**
 * The boot screen displays some strange hex codes, garbage from the graphics memory
 * and eventually a grid (maybe used to calibrate the screen?). This scene tries to mimic that to a certain degree.
 */
public class Arcade_BootScene2D extends GameScene {

    public static final int TILE_WIDTH = 28;
    public static final int TILE_HEIGHT = 36;

    private static final Renderable BLANK_CANVAS = new BlankCanvas();

    public enum SceneState {
        EMPTINESS(0),
        HEX_CODES(60),
        SPRITE_NOISE(120),
        GRID(210),
        EXPIRATION(240);

        SceneState(int startTick) {
            this.startTick = startTick;
        }

        public int startTick() {
            return startTick;
        }

        private final int startTick;
    }

    public SceneState currentState;

    private Renderable currentRenderable;

    public Arcade_BootScene2D(GameAppContext app) {
        super(app);
        setComp(SceneCanvasRenderingComp.class, new SceneCanvasRenderingComp());
    }

    @Override
    public boolean wantsClearCanvas() {
        return false;
    }

    @Override
    public Stream<Renderable> renderables() {
        return Ufx.streamOf(currentRenderable);
    }

    @Override
    public void onActivate() {
        currentState = SceneState.EMPTINESS;
        currentRenderable = BLANK_CANVAS;

        game().session().setHudVisible(false);

        //TODO This is only a temporary solution
        setHUDStyle(game().session().hud());
    }

    @Override
    public void onTick(GameContext game) {

        if (game.state().timer().hasExpired()) {
            return;
        }

        if (currentState == SceneState.EXPIRATION) {
            game().state().timer().expire();
            return;
        }

        final long t = game().state().timer().tickCount();
        final int byFour = (int) t % 4;

        // Start next state?
        for (var nextState : SceneState.values()) {
            if (t == nextState.startTick()) {
                currentState = nextState;
            }
        }

        switch (currentState) {
            case HEX_CODES -> {
                switch (byFour) {
                    case 0 -> currentRenderable = new RandomHexCodeBlock(TILE_WIDTH, TILE_HEIGHT);
                    case 3 -> currentRenderable = BLANK_CANVAS;
                }
            }
            case SPRITE_NOISE -> {
                switch (byFour) {
                    case 0 -> currentRenderable = new SpriteNoise(TILE_WIDTH, TILE_HEIGHT);
                    case 3 -> currentRenderable = BLANK_CANVAS;
                }
            }
            case GRID -> {
                switch (byFour) {
                    case 0 -> {
                        if (!(currentRenderable instanceof GridPattern)) {
                            currentRenderable = BLANK_CANVAS;
                        }
                    }
                    case 1 -> {
                        if (currentRenderable == BLANK_CANVAS) {
                            currentRenderable = new GridPattern(TILE_WIDTH, TILE_HEIGHT);
                        }
                    }
                }
            }
            default -> currentRenderable = BLANK_CANVAS;
        }
    }

    private void setHUDStyle(HUD hud) {
        final HUD_Style hudStyle = app().currentGameVariantUIConfig().renderConfig().hudStyle();
        hud.levelCounter().setComp(HUD_Style.class, hudStyle);
        hud.livesCounter().setComp(HUD_Style.class, hudStyle);
        hud.gameScore().setComp(HUD_Style.class, hudStyle);
        hud.highScore().setComp(HUD_Style.class, hudStyle);
        hud.creditDisplay().setComp(HUD_Style.class, hudStyle);
    }
}
