/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_GameRenderer;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.GameUI_Config;
import de.amr.pacmanfx.ui.PacManGames_Assets;
import de.amr.pacmanfx.ui._2d.GenericMapRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

public class PacManXXL_MsPacMan_GameRenderer extends ArcadeMsPacMan_GameRenderer {

    private final GenericMapRenderer mapRenderer;

    public PacManXXL_MsPacMan_GameRenderer(PacManGames_Assets assets, GameUI_Config config, Canvas canvas) {
        super(assets, config, canvas);
        mapRenderer = new GenericMapRenderer(canvas);
        mapRenderer.scalingProperty().bind(scalingProperty());
    }

    @Override
    public void drawLevel(
        GameContext gameContext,
        GameLevel level,
        Color backgroundColor,
        boolean mazeHighlighted,
        boolean energizerHighlighted,
        long tick)
    {
        mapRenderer.drawLevel(level, mazeHighlighted, energizerHighlighted);
    }
}
