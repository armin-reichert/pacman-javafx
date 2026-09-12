/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes.bootscene;

import de.amr.basics.timer.TickTimer;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.SceneCanvasRenderingComp;
import de.amr.pacmanfx.uilib.entities.hud.comp.HUD_Style;

import java.util.stream.Stream;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;

/**
 * The boot screen displays some strange hex codes, garbage from the graphics memory
 * and eventually a grid (maybe used to calibrate the screen?). This scene tries to mimic that to a certain degree.
 */
public class Arcade_BootScene extends GameScene {

    public static final int WIDTH_IN_TILES  = 28;
    public static final int HEIGHT_IN_TILES = 36;

    private static final Renderable BLANK_CANVAS = new BlankCanvas();
    private static final Renderable GRID = new GridPattern(WIDTH_IN_TILES, HEIGHT_IN_TILES);

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

    private Renderable renderable;

    public Arcade_BootScene(GameAppContext app) {
        super(app);

        final var rendering = new SceneCanvasRenderingComp();
        rendering.setUnscaledWidth(WIDTH_IN_TILES * TS);
        rendering.setUnscaledHeight(HEIGHT_IN_TILES * TS);
        setComp(SceneCanvasRenderingComp.class, rendering);
    }

    @Override
    public boolean wantsClearCanvas() {
        return false;
    }

    @Override
    public Stream<Renderable> renderables() {
        return Ufx.streamOf(renderable);
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
            case DARK -> renderable = BLANK_CANVAS;

            case HEX_CODES -> {
                if (mod4 == 0) {
                    renderable = BLANK_CANVAS;
                } else if (mod4 == 1) {
                    renderable = new HexDigitsBlock(WIDTH_IN_TILES, HEIGHT_IN_TILES);
                }
            }

            case SPRITE_NOISE -> {
                if (mod4 == 0) {
                    renderable = BLANK_CANVAS;
                } else if (mod4 == 1) {
                    renderable = new SpritesBlock(WIDTH_IN_TILES, HEIGHT_IN_TILES);
                }
            }

            case GRID -> {
                if (t == currentState.startTick()) {
                    renderable = BLANK_CANVAS;
                } else {
                    renderable = GRID;
                }
            }

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
