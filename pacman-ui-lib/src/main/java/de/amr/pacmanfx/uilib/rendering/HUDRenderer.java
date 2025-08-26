/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.HUD;

public interface HUDRenderer extends CanvasRenderer {

    /**
     * Draws the Head-Up Display (score, live counter, level counter, coins inserted)
     *
     * @param game the game model
     * @param hud the data displayed in the Head-Up Display
     * @param sceneSize scene size in pixels
     */
    void drawHUD(Game game, HUD hud, Vector2f sceneSize);
}
