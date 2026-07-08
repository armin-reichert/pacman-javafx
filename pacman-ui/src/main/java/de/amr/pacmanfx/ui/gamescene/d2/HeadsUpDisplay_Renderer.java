/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.model.HUDState;
import de.amr.pacmanfx.uilib.rendering.Renderer;

public interface HeadsUpDisplay_Renderer extends Renderer {

    /**
     * Draws the Head-Up Display (score, live counter, level counter, coins inserted)
     *
     * @param hud the HUD
     * @param context the game context
     * @param scene 2D scene
     * @param tick current tick of the game machine clock
     */
    void draw(HUDState hud, GameContext context, AbstractGameScene2D scene, long tick);
}
