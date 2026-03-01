/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d2;

import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.HeadsUpDisplay;
import de.amr.pacmanfx.uilib.rendering.Renderer;

public interface HeadsUpDisplay_Renderer extends Renderer {

    /**
     * Draws the Head-Up Display (score, live counter, level counter, coins inserted)
     *
     * @param hud the HUD
     * @param game the game model
     * @param scene 2D scene
     */
    void draw(HeadsUpDisplay hud, Game game, GameScene2D scene);
}
