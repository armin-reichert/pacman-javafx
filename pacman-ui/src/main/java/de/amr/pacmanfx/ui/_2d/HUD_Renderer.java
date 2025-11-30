/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.uilib.rendering.Renderer;

public interface HUD_Renderer extends Renderer {

    /**
     * Draws the Head-Up Display (score, live counter, level counter, coins inserted)
     *
     * @param game the game model
     * @param scene 2D scene
     */
    void drawHUD(Game game, GameScene2D scene);
}
