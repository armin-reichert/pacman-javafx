/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_GameLevelRenderer;
import de.amr.pacmanfx.ui._2d.GenericMapRenderer;
import de.amr.pacmanfx.uilib.rendering.RenderInfo;
import javafx.scene.canvas.Canvas;

/**
 * Renderer for "Pac-Man XXL" game variant. Uses the vector graphics map renderer that can render any custom map.
 */
public class PacManXXL_PacMan_GameLevelRenderer extends ArcadePacMan_GameLevelRenderer {

    private final GenericMapRenderer mapRenderer;

    public PacManXXL_PacMan_GameLevelRenderer(Canvas canvas, PacManXXL_PacMan_UIConfig uiConfig) {
        super(canvas, uiConfig);
        mapRenderer = new GenericMapRenderer(canvas);
        mapRenderer.scalingProperty().bind(scalingProperty());
    }

    @Override
    public void drawGameLevel(GameContext context, RenderInfo info) {
        mapRenderer.drawLevel(context.gameLevel(),
            info.getBoolean("mazeBright"),
            info.getBoolean("blinkingPhaseOn")
        );
        super.drawGameLevelMessage(context.gameLevel());
    }
}