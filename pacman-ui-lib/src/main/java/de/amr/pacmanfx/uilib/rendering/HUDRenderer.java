package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.HUDData;

public interface HUDRenderer {

    /**
     * Draws the Head-Up Display (score, live counter, level counter, coins inserted)
     *
     * @param gameContext the game context
     * @param data the data displayed in the Head-Up Display
     * @param sceneSize scene size in pixels
     */
    void drawHUD(GameContext gameContext, HUDData data, Vector2f sceneSize);
}
