/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.gameplay.FrameContext;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameExtension;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;

import static de.amr.pacmanfx.tengenmspacman.config.TengenMsPacManGameVariant.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.config.TengenMsPacManGameVariant.NES_SCREEN_WIDTH;

/**
 * Gives credit to the people that helped in making the game, original and remake authors.
 */
public class TengenMsPacMan_CreditsScene extends AbstractGameScene2D {

    public static final int DISPLAY_TICKS = 16 * 60;

    public enum DisplayMode { ORIGINAL_AUTHORS, REMAKE_AUTHORS }

    public DisplayMode displayMode = DisplayMode.ORIGINAL_AUTHORS;
    public float fadeProgress = 0;

    public TengenMsPacMan_CreditsScene(GameAppContext appContext) {
        super(appContext);
        unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        unscaledHeightProperty().set(NES_SCREEN_HEIGHT);
    }

    @Override
    public void onActivate() {
        final var actions = appContext().getExtensionValue(TengenMsPacMan_GameExtension.ACTIONS, TengenMsPacMan_Actions.class);

        actionBindings().selectAnyMatchingBinding(actions.actionEnterStartScreen(), actions.localBindings());

        fadeProgress = 0;
        displayMode = DisplayMode.ORIGINAL_AUTHORS;

        gameModel().hudState().hide();
    }

    @Override
    public void onTick(FrameContext frame) {
        final TickTimer stateTimer = gameState().timer();
        if (stateTimer.tickCount() == DISPLAY_TICKS) {
            gameState().triggerTimeout();
            return;
        }
        if (stateTimer.tickCount() == DISPLAY_TICKS / 2) {
            displayMode = DisplayMode.REMAKE_AUTHORS;
        }
        if (stateTimer.tickCount() > DISPLAY_TICKS / 2) {
            fadeProgress = Math.min(fadeProgress + 0.005f, 1f); // Clamp to 1.0
        }
    }
}