/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.basics.fsm.State;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.d2.GameScene2D;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_ActionBindings.TENGEN_SPECIFIC_BINDINGS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions.ACTION_ENTER_START_SCREEN;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_WIDTH;

/**
 * Gives credit to the people that helped in making the game, original and remake authors.
 */
public class TengenMsPacMan_CreditsScene extends GameScene2D {

    public static final float DISPLAY_SECONDS = 16;

    public float fadeProgress = 0;

    public TengenMsPacMan_CreditsScene(AppContext context) {
        super(context);
        unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        unscaledHeightProperty().set(NES_SCREEN_HEIGHT);
    }

    @Override
    public void onActivate(AppContext context) {
        context.currentGameContext().gameModel().hud().hide();
        actionBindings.registerFirstBinding(ACTION_ENTER_START_SCREEN, TENGEN_SPECIFIC_BINDINGS);
        fadeProgress = 0;
    }

    @Override
    public void onTick(long tick) {
        final State<GameContext> gameState = appContext.currentGameContext().gameState();
        if (gameState.timer().atSecond(DISPLAY_SECONDS)) {
            gameState.expire();
            return;
        }
        if (gameState.timer().betweenSeconds(0.5 * DISPLAY_SECONDS, DISPLAY_SECONDS)) {
            fadeProgress = Math.min(fadeProgress + 0.005f, 1f); // Clamp to 1.0
        }
    }
}