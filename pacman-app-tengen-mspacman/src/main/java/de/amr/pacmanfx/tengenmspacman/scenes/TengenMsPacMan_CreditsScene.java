/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui._2d.GameScene2D;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_ActionBindings.TENGEN_SPECIFIC_BINDINGS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions.ACTION_ENTER_START_SCREEN;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;

/**
 * Gives credit to the people that helped in making the game, original and remake authors.
 */
public class TengenMsPacMan_CreditsScene extends GameScene2D {

    public static final float DISPLAY_SECONDS = 16;

    public float fadeProgress = 0;

    public TengenMsPacMan_CreditsScene() {}

    @Override
    protected void doInit(Game game) {
        game.hud().hide();
        actionBindings.registerAnyFrom(ACTION_ENTER_START_SCREEN, TENGEN_SPECIFIC_BINDINGS);
        fadeProgress = 0;
    }

    @Override
    protected void doEnd(Game game) {}

    @Override
    public void update(Game game) {
        if (game.control().state().timer().atSecond(DISPLAY_SECONDS)) {
            game.control().terminateGameState();
            return;
        }
        if (game.control().state().timer().betweenSeconds(0.5 * DISPLAY_SECONDS, DISPLAY_SECONDS)) {
            fadeProgress = Math.min(fadeProgress + 0.005f, 1f); // Clamp to 1.0
        }
    }

    @Override
    public Vector2i unscaledSize() { return NES_SIZE_PX; }
}