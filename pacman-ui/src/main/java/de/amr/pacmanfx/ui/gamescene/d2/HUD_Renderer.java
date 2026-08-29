/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.uilib.rendering.Renderer;

public interface HUD_Renderer extends Renderer {

    /**
     * Draws the Head-Up Display (score, live counter, level counter, coins inserted)
     *
     * @param session the running game session
     * @param gameScene 2D scene
     * @param tick current tick of the game machine clock
     */
    void drawHUD(GameSession session, GameScene gameScene, long tick);
}
