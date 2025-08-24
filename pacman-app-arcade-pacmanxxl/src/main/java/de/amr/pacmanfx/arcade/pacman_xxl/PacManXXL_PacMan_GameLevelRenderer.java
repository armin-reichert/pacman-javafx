/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_GameLevelRenderer;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui._2d.GenericMapRenderer;
import de.amr.pacmanfx.uilib.rendering.RenderInfo;
import javafx.scene.canvas.Canvas;

/**
 * Renderer for "Pac-Man XXL" game variant. Uses the vector graphics map renderer that can render any custom map.
 */
public class PacManXXL_PacMan_GameLevelRenderer extends ArcadePacMan_GameLevelRenderer {

    private final GenericMapRenderer mazeRenderer;

    public PacManXXL_PacMan_GameLevelRenderer(Canvas canvas, PacManXXL_PacMan_UIConfig uiConfig) {
        super(canvas, uiConfig);
        mazeRenderer = new GenericMapRenderer(canvas);
        mazeRenderer.scalingProperty().bind(scalingProperty());
        mazeRenderer.backgroundColorProperty().bind(backgroundColorProperty());
    }

    @Override
    protected void drawMaze(GameLevel gameLevel, RenderInfo info) {
        mazeRenderer.drawMaze(gameLevel, info);
    }
}