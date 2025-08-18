package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.HUDData;
import de.amr.pacmanfx.uilib.GameClock;

public interface HUDRenderer {

    /**
     * Draws the Head-Up Display (score, live counter, level counter, coins inserted)
     *
     * @param gameContext the game context
     * @param gameClock the game clock (used for blinking effects)
     * @param data the data displayed in the Head-Up Display
     * @param sceneSize scene size in pixels
     */
    void drawHUD(GameContext gameContext, GameClock gameClock, HUDData data, Vector2f sceneSize);
}
