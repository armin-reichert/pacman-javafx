/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.model.GameLevel;

public interface GameLevelRenderer extends Renderer {

    /**
     * Applies settings specific to the given game level to this renderer. This can be for example
     * the selection of a different color scheme which is specified in the level map.
     *
     * @param level the game level
     * @param info rendering info
     */
    void applyLevelSettings(GameLevel level, RenderInfo info);

    /**
     * @param level the game level to draw
     * @param info additional rendering info
     */
    void drawLevel(GameLevel level, RenderInfo info);
}
