/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;

public interface HUD_Renderer extends Renderer {

    /**
     * Draws the Head-Up Display (score, live counter, level counter, coins inserted)
     *
     * @param game the game model
     * @param sceneSize scene size in pixels
     */
    void drawHUD(Game game, Vector2i sceneSize);
}
