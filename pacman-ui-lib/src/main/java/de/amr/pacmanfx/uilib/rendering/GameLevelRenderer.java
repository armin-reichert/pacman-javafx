/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.rules.GameRules;

public interface GameLevelRenderer extends Renderer {

    /**
     * Applies settings specific to the given game level to this renderer. This can be for example
     * the selection of a different color scheme which is specified in the level map.
     *
     * @param rules game rules for the current variant
     * @param level the game level
     * @param info rendering info
     */
    void applyLevelSettings(GameRules rules, GameLevel level, RenderInfo info);

    /**
     * @param game the current game
     * @param level the game level to draw
     * @param info additional rendering info
     */
    void drawLevel(GameContext game, GameLevel level, RenderInfo info);
}
