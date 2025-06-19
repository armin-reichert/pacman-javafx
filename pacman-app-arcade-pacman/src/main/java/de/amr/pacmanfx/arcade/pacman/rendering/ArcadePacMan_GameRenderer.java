/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.ui._2d.SpriteGameRenderer;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Globals.theGameLevel;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static de.amr.pacmanfx.ui.PacManGames.theAssets;
import static de.amr.pacmanfx.ui.PacManGames.theUI;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class ArcadePacMan_GameRenderer extends SpriteGameRenderer {

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
    public void drawHUD(GameModel game) {
        requireNonNull(game);

        final HUD hud = game.hud();
        if (!hud.isVisible()) return;

        Vector2f sceneSize = ARCADE_MAP_SIZE_IN_PIXELS;
        if (optGameLevel().isPresent()) {
            int numRows = theGameLevel().worldMap().numRows();
            int numCols = theGameLevel().worldMap().numCols();
            sceneSize = new Vector2f(numCols * TS, numRows * TS);
        }

        if (hud.isScoreVisible()) {
            Color scoreColor = theAssets().color(theUI().configuration().assetNamespace() + ".color.score");
            Font scoreFont = theAssets().arcadeFont(8);
            drawScore(game.score(), "SCORE", tiles_to_px(1), tiles_to_px(1), scoreFont, scoreColor);
            drawScore(game.highScore(), "HIGH SCORE", tiles_to_px(14), tiles_to_px(1), scoreFont, scoreColor);
        }

        if (hud.isLevelCounterVisible()) {
            LevelCounter levelCounter = hud.levelCounter();
            float x = sceneSize.x() - 4 * TS, y = sceneSize.y() - 2 * TS;
            for (byte symbol : levelCounter.symbols()) {
                Sprite sprite = theUI().configuration().createBonusSymbolSprite(symbol);
                drawSpriteScaled(sprite, x, y);
                x -= TS * 2;
            }
        }

        if (hud.isLivesCounterVisible()) {
            LivesCounter livesCounter = hud.livesCounter();
            Sprite sprite = theUI().configuration().createLivesCounterSprite();
            for (int i = 0; i < livesCounter.visibleLifeCount(); ++i) {
                drawSpriteScaled(sprite, livesCounter.x() + TS * (2 * i), livesCounter.y());
            }
            if (game.lifeCount() > livesCounter.maxLivesDisplayed()) {
                // show text indicating that more lives are available than symbols displayed (cheating may cause this)
                Font font = Font.font("Serif", FontWeight.BOLD, scaled(8));
                fillTextAtScaledPosition("(%d)".formatted(game.lifeCount()), Color.YELLOW, font,
                    livesCounter.x() + TS * 10, livesCounter.y() + TS);
            }
        }
    }

    private void drawScore(Score score, String title, double x, double y, Font font, Color color) {
        fillTextAtScaledPosition(title, color, font, x, y);
        fillTextAtScaledPosition("%7s".formatted("%02d".formatted(score.points())), color, font, x, y + TS + 1);
        if (score.points() != 0) {
            fillTextAtScaledPosition("L" + score.levelNumber(), color, font, x + tiles_to_px(8), y + TS + 1);
        }
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