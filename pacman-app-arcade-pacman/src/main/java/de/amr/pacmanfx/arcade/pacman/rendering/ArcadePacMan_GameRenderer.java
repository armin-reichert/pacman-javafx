/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.ui._2d.DebugInfoRenderer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.GameClock;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.GameRenderer;
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

/**
 * Renderer for classic Arcade Pac-Man and Pac-Man XXL game variants.
 */
public class ArcadePacMan_GameRenderer extends GameRenderer implements DebugInfoRenderer {

    protected final GameUI_Config uiConfig;
    protected final ArcadePacMan_SpriteSheet spriteSheet;

    public ArcadePacMan_GameRenderer(GameUI_Config uiConfig, Canvas canvas, ArcadePacMan_SpriteSheet spriteSheet) {
        this.uiConfig = requireNonNull(uiConfig);
        this.spriteSheet = requireNonNull(spriteSheet);
        setCanvas(canvas);
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public void drawHUD(GameContext gameContext, GameClock gameClock, HUDData data, Vector2f sceneSize) {
        if (!data.isVisible()) return;

        Font font = uiConfig.theUI().assets().arcadeFont(scaled(TS));

        if (data.isScoreVisible()) {
            ScoreManager scoreManager = gameContext.game().scoreManager();
            drawScore(scoreManager.score(), "SCORE", ARCADE_WHITE, font, TS(1), TS(1));
            drawScore(scoreManager.highScore(), "HIGH SCORE", ARCADE_WHITE, font, TS(14), TS(1));
        }

        if (data.isLevelCounterVisible()) {
            LevelCounter levelCounter = data.theLevelCounter();
            RectShort[] bonusSymbols = spriteSheet.spriteSequence(SpriteID.BONUS_SYMBOLS);
            float x = sceneSize.x() - TS(4), y = sceneSize.y() - TS(2) + 2;
            for (byte symbol : levelCounter.symbols()) {
                drawSprite(spriteSheet, bonusSymbols[symbol], x, y, true);
                x -= TS(2); // symbols are drawn from right to left
            }
        }

        if (data.isLivesCounterVisible()) {
            LivesCounter livesCounter = data.theLivesCounter();
            RectShort sprite = spriteSheet.sprite(SpriteID.LIVES_COUNTER_SYMBOL);
            float x = TS(2), y = sceneSize.y() - TS(2);
            for (int i = 0; i < livesCounter.visibleLifeCount(); ++i) {
                drawSprite(spriteSheet, sprite, x + i * TS(2), y, true);
            }
            int lifeCount = gameContext.game().lifeCount();
            if (lifeCount > livesCounter.maxLivesDisplayed()) {
                // show text indicating that more lives are available than symbols displayed (cheating may cause this)
                Font hintFont = Font.font("Serif", FontWeight.BOLD, scaled(8));
                fillText("%d".formatted(lifeCount), ARCADE_YELLOW, hintFont, x - 14, y + TS);
            }
        }

        if (data.isCreditVisible()) {
            int credit = gameContext.coinMechanism().numCoins();
            fillText("CREDIT %2d".formatted(credit), ARCADE_WHITE, font, TS(2), sceneSize.y());
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
    public void drawLevel(GameContext gameContext, GameClock gameClock, Color backgroundColor, boolean mazeBright, boolean energizerBright) {
        GameLevel gameLevel = gameContext.gameLevel();
        ctx().save();
        ctx().scale(scaling(), scaling());
        if (mazeBright) {
            Image brightMazeImage = uiConfig.assets().image("flashing_maze");
            ctx().drawImage(brightMazeImage, 0, GameLevel.EMPTY_ROWS_OVER_MAZE * TS);
        }
        else if (gameLevel.uneatenFoodCount() == 0) {
            drawSprite(spriteSheet, spriteSheet.sprite(SpriteID.MAP_EMPTY), 0, TS(GameLevel.EMPTY_ROWS_OVER_MAZE), false);
        }
        else {
            drawSprite(spriteSheet, spriteSheet.sprite(SpriteID.MAP_FULL), 0, TS(GameLevel.EMPTY_ROWS_OVER_MAZE), false);
            ctx().setFill(backgroundColor);
            gameLevel.worldMap().tiles()
                    .filter(not(gameLevel::isEnergizerPosition))
                    .filter(gameLevel::tileContainsEatenFood)
                    .forEach(tile -> fillSquareAtTileCenter(tile, 4));
            gameLevel.energizerPositions().stream()
                    .filter(tile -> !energizerBright || gameLevel.tileContainsEatenFood(tile))
                    .forEach(tile -> fillSquareAtTileCenter(tile, 10));
        }
        ctx().restore();
    }

    @Override
    public void drawActor(Actor actor, SpriteSheet<?> spriteSheet) {
        if (actor instanceof Bonus bonus) {
            drawBonus(bonus);
        }
        else super.drawActor(actor, spriteSheet);
    }

    private void drawBonus(Bonus bonus) {
        switch (bonus.state()) {
            case EDIBLE -> drawSpriteCentered(bonus.center(),
                spriteSheet.spriteSequence(SpriteID.BONUS_SYMBOLS)[bonus.symbol()]);
            case EATEN  -> drawSpriteCentered(bonus.center(),
                spriteSheet.spriteSequence(SpriteID.BONUS_VALUES)[bonus.symbol()]);
            case INACTIVE -> {}
        }
    }
}