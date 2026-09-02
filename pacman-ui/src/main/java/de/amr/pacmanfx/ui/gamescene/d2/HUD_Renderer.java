/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.uilib.rendering.Renderer;

public interface HUD_Renderer extends Renderer {

    /**
     * Draws the Head-Up Display (score, live counter, level counter, coins inserted)
     *
     * @param hud the HUD
     * @param session current game session
     * @param gameScene current game scene
     * @param tick current tick of the game machine clock
     */
    void drawHUD(HUD hud, GameSession session, GameScene gameScene, long tick);
}
