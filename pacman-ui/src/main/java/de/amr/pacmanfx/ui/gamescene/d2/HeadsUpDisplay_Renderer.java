/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.pacmanfx.core.session.GameSession;
import de.amr.pacmanfx.uilib.rendering.Renderer;

public interface HeadsUpDisplay_Renderer extends Renderer {

    /**
     * Draws the Head-Up Display (score, live counter, level counter, coins inserted)
     *
     * @param session the running game session
     * @param scene 2D scene
     * @param tick current tick of the game machine clock
     */
    void draw(GameSession session, AbstractGameScene2D scene, long tick);
}
