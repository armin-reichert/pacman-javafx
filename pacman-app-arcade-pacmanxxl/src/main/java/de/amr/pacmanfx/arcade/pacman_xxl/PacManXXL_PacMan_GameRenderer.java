/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_GameRenderer;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui._2d.GenericMapRenderer;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

public class PacManXXL_PacMan_GameRenderer extends ArcadePacMan_GameRenderer {

    private final GenericMapRenderer mapRenderer;

    public PacManXXL_PacMan_GameRenderer(
        AssetStorage assets,
        ArcadePacMan_SpriteSheet spriteSheet,
        Canvas canvas)
    {
        super(assets, spriteSheet, canvas);
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