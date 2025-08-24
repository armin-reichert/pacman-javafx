/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_GameLevelRenderer;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui._2d.GenericMapRenderer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.rendering.RenderInfo;
import javafx.scene.canvas.Canvas;

public class PacManXXL_MsPacMan_GameLevelRenderer extends ArcadeMsPacMan_GameLevelRenderer {

    private final GenericMapRenderer mazeRenderer;

    public PacManXXL_MsPacMan_GameLevelRenderer(Canvas canvas, GameUI_Config uiConfig) {
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
