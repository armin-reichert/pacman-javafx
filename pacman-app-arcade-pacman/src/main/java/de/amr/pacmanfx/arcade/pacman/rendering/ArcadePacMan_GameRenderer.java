/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui._2d.SpriteGameRenderer;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.PacManGames.theAssets;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class ArcadePacMan_GameRenderer implements SpriteGameRenderer {

    private final ArcadePacMan_SpriteSheet spriteSheet;
    private final FloatProperty scalingPy = new SimpleFloatProperty(1.0f);
    private final GraphicsContext ctx;

    public ArcadePacMan_GameRenderer(ArcadePacMan_SpriteSheet spriteSheet, Canvas canvas) {
        this.spriteSheet = requireNonNull(spriteSheet);
        ctx = requireNonNull(canvas).getGraphicsContext2D();
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public GraphicsContext ctx() {
        return ctx;
    }

    @Override
    public FloatProperty scalingProperty() {
        return scalingPy;
    }

    @Override
    public void drawLevel(GameLevel level, Color backgroundColor, boolean mazeHighlighted, boolean energizerHighlighted) {
        ctx.save();
        ctx.scale(scaling(), scaling());
        if (mazeHighlighted) {
            ctx.drawImage(theAssets().image("pacman.flashing_maze"), 0, GameLevel.EMPTY_ROWS_OVER_MAZE * TS);
        }
        else if (level.uneatenFoodCount() == 0) {
            drawSprite(spriteSheet.sprite(SpriteID.MAP_EMPTY), 0, GameLevel.EMPTY_ROWS_OVER_MAZE * TS);
        }
        else {
            drawSprite(spriteSheet.sprite(SpriteID.MAP_FULL), 0, GameLevel.EMPTY_ROWS_OVER_MAZE * TS);
            ctx.setFill(backgroundColor);
            level.worldMap().tiles()
                    .filter(not(level::isEnergizerPosition))
                    .filter(level::tileContainsEatenFood)
                    .forEach(tile -> fillSquareAtTileCenter(tile, 4));
            level.energizerTiles()
                    .filter(tile -> !energizerHighlighted || level.tileContainsEatenFood(tile))
                    .forEach(tile -> fillSquareAtTileCenter(tile, 10));
        }
        ctx.restore();
    }
}