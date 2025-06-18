/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui._2d.GenericMapRenderer;
import de.amr.pacmanfx.ui._2d.SpriteGameRenderer;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static java.util.Objects.requireNonNull;

public class PacManXXL_PacMan_GameRenderer implements SpriteGameRenderer {

    private final FloatProperty scalingPy = new SimpleFloatProperty(1.0f);
    private final GraphicsContext ctx;
    private final ArcadePacMan_SpriteSheet spriteSheet;
    private final GenericMapRenderer mapRenderer;

    public PacManXXL_PacMan_GameRenderer(ArcadePacMan_SpriteSheet spriteSheet, Canvas canvas) {
        this.spriteSheet = requireNonNull(spriteSheet);
        ctx = requireNonNull(canvas).getGraphicsContext2D();
        mapRenderer = new GenericMapRenderer(canvas);
        mapRenderer.scalingProperty().bind(scalingProperty());
    }

    @Override
    public FloatProperty scalingProperty() {
        return scalingPy;
    }

    @Override
    public GraphicsContext ctx() {
        return ctx;
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public void drawLevel(GameLevel level, Color backgroundColor, boolean mazeHighlighted, boolean energizerHighlighted) {
        mapRenderer.drawLevel(level, mazeHighlighted, energizerHighlighted);
    }
}
