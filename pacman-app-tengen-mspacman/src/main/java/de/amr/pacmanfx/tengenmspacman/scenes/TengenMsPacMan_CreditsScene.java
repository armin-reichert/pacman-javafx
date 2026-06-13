/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.d2.GameScene2D;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_WIDTH;

/**
 * Gives credit to the people that helped in making the game, original and remake authors.
 */
public class TengenMsPacMan_CreditsScene extends GameScene2D {

    public static final float DISPLAY_SECONDS = 16;

    public float fadeProgress = 0;

    public TengenMsPacMan_CreditsScene(Game game) {
        super(game);
        unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        unscaledHeightProperty().set(NES_SCREEN_HEIGHT);
    }

    @Override
    public void onActivate() {
        final TengenMsPacMan_Actions actions = game().ui().extensions().getExtension(
            TengenMsPacMan_UIConfig.EXT_ACTIONS, TengenMsPacMan_Actions.class);

        gameModel().hud().hide();
        actionBindings().selectAnyMatchingBinding(actions.actionEnterStartScreen(), actions.localBindings());
        fadeProgress = 0;
    }

    @Override
    public void onTick(long tick) {
        if (gameState().timer().atSecond(DISPLAY_SECONDS)) {
            gameState().expire();
            return;
        }
        if (gameState().timer().betweenSeconds(0.5 * DISPLAY_SECONDS, DISPLAY_SECONDS)) {
            fadeProgress = Math.min(fadeProgress + 0.005f, 1f); // Clamp to 1.0
        }
    }
}