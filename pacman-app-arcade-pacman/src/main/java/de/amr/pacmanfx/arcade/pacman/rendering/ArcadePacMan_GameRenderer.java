/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.ui.GameAssets;
import de.amr.pacmanfx.ui._2d.GameRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.ARCADE_WHITE;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.ARCADE_YELLOW;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class ArcadePacMan_GameRenderer extends GameRenderer {

    protected final GameAssets assets;
    protected ArcadePacMan_SpriteSheet spriteSheet;

    public ArcadePacMan_GameRenderer(GameAssets assets, Canvas canvas, ArcadePacMan_SpriteSheet spriteSheet) {
        this.assets = requireNonNull(assets);
        this.spriteSheet = requireNonNull(spriteSheet);
        setCanvas(canvas);
    }

    @Override
    public void drawHUD(GameContext gameContext, HUDData data, Vector2f sceneSize, long tick) {
        if (!data.isVisible()) return;

        Font font8 = assets.arcadeFont(scaled(8));
        if (data.isScoreVisible()) {
            drawScore(gameContext.game().scoreManager().score(), "SCORE", ARCADE_WHITE, font8, TS(1), TS(1));
            drawScore(gameContext.game().scoreManager().highScore(), "HIGH SCORE", ARCADE_WHITE, font8, TS(14), TS(1));
        }

        if (data.isLevelCounterVisible()) {
            LevelCounter levelCounter = data.theLevelCounter();
            float x = sceneSize.x() - TS(4), y = sceneSize.y() - TS(2) + 2;
            for (byte symbol : levelCounter.symbols()) {
                RectShort sprite = spriteSheet.spriteSequence(SpriteID.BONUS_SYMBOLS)[symbol];
                drawSprite(spriteSheet.sourceImage(), sprite, x, y, true);
                x -= TS(2);
            }
        }

        if (data.isLivesCounterVisible()) {
            LivesCounter livesCounter = data.theLivesCounter();
            float x = TS(2), y = sceneSize.y() - TS(2);
            RectShort sprite = spriteSheet.sprite(SpriteID.LIVES_COUNTER_SYMBOL);
            for (int i = 0; i < livesCounter.visibleLifeCount(); ++i) {
                drawSprite(spriteSheet.sourceImage(), sprite, x + TS(2 * i), y, true);
            }
            if (gameContext.game().lifeCount() > livesCounter.maxLivesDisplayed()) {
                // show text indicating that more lives are available than symbols displayed (cheating may cause this)
                Font font = Font.font("Serif", FontWeight.BOLD, scaled(8));
                fillText("%d".formatted(gameContext.game().lifeCount()), ARCADE_YELLOW, font, x - 14, y + TS);
            }
        }

        if (data.isCreditVisible()) {
            String text = "CREDIT %2d".formatted(gameContext.coinMechanism().numCoins());
            Font font = assets.arcadeFont(scaled(8));
            fillText(text, ARCADE_WHITE, font, TS(2), sceneSize.y());
        }
    }

    private void drawScore(Score score, String title, Color color, Font font, double x, double y) {
        fillText(title, color, font, x, y);
        fillText("%7s".formatted("%02d".formatted(score.points())), color, font, x, y + TS + 1);
        if (score.points() != 0) {
            fillText("L" + score.levelNumber(), color, font, x + TS(8), y + TS + 1);
        }
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
        ctx().save();
        ctx().scale(scaling(), scaling());
        if (mazeHighlighted) {
            Image flashingMaze = assets.image(ArcadePacMan_UIConfig.ASSET_NAMESPACE + ".flashing_maze");
            ctx().drawImage(flashingMaze, 0, GameLevel.EMPTY_ROWS_OVER_MAZE * TS);
        }
        else if (level.uneatenFoodCount() == 0) {
            drawSprite(spriteSheet.sourceImage(), spriteSheet.sprite(SpriteID.MAP_EMPTY), 0, TS(GameLevel.EMPTY_ROWS_OVER_MAZE), false);
        }
        else {
            drawSprite(spriteSheet.sourceImage(), spriteSheet.sprite(SpriteID.MAP_FULL), 0, TS(GameLevel.EMPTY_ROWS_OVER_MAZE), false);
            ctx().setFill(backgroundColor);
            level.worldMap().tiles()
                    .filter(not(level::isEnergizerPosition))
                    .filter(level::tileContainsEatenFood)
                    .forEach(tile -> fillSquareAtTileCenter(tile, 4));
            level.energizerPositions().stream()
                    .filter(tile -> !energizerHighlighted || level.tileContainsEatenFood(tile))
                    .forEach(tile -> fillSquareAtTileCenter(tile, 10));
        }
        ctx().restore();
    }

    @Override
    public void drawActor(Actor actor, Image spriteSheetImage) {
        if (actor instanceof Bonus bonus) {
            drawBonus(bonus);
        }
        else super.drawActor(actor, spriteSheetImage);
    }

    private void drawBonus(Bonus bonus) {
        switch (bonus.state()) {
            case EDIBLE -> drawActorSprite(bonus, spriteSheet.sourceImage(), spriteSheet.spriteSequence(SpriteID.BONUS_SYMBOLS)[bonus.symbol()]);
            case EATEN  -> drawActorSprite(bonus, spriteSheet.sourceImage(), spriteSheet.spriteSequence(SpriteID.BONUS_VALUES)[bonus.symbol()]);
            case INACTIVE -> {}
        }
    }
}