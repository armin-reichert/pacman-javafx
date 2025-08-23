/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.GameContext;
import javafx.scene.canvas.Canvas;

public abstract class GameLevelRenderer extends BaseRenderer {

    public record RenderingInfo(boolean mazeBright, boolean blinkingPhaseOn) {}

    public GameLevelRenderer(Canvas canvas) {
        super(canvas);
    }

    /**
     * Applies settings specific to the given game level to this renderer. This can be for example
     * the selection of a different color scheme which is specified in the level map. The default
     * implementation is empty such that subclasses that have no such hints can silently ignore it.
     *
     * @param gameContext the game context
     */
    public void applyLevelSettings(GameContext gameContext) {}

    /**
     * @param gameContext the game context
     * @param renderingInfo additional rendering info
     */
    public abstract void drawGameLevel(GameContext gameContext, RenderingInfo renderingInfo);
}
