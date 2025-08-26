/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.HUDControlData;
import de.amr.pacmanfx.model.ScoreManager;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.*;
import de.amr.pacmanfx.uilib.GameClock;
import de.amr.pacmanfx.uilib.rendering.BaseSpriteRenderer;
import de.amr.pacmanfx.uilib.rendering.HUDRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesColor;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_HUDRenderer extends BaseSpriteRenderer implements HUDRenderer {

    private final TengenMsPacMan_UIConfig uiConfig;
    private final GameClock clock;

    public TengenMsPacMan_HUDRenderer(Canvas canvas, TengenMsPacMan_UIConfig uiConfig, GameClock clock) {
        super(canvas);
        this.uiConfig = requireNonNull(uiConfig);
        this.clock = requireNonNull(clock);
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return uiConfig.spriteSheet();
    }

    @Override
    public void drawHUD(GameContext gameContext, HUDControlData data, Vector2f sceneSize) {
        requireNonNull(gameContext);
        requireNonNull(data);
        requireNonNull(sceneSize);

        TengenMsPacMan_GameModel game = gameContext.game();
        GameLevel gameLevel = game.optGameLevel().orElse(null);

        if (gameLevel == null) {
            return; // should never happen
        }

        TengenMsPacMan_HUDControlData hudControlData = (TengenMsPacMan_HUDControlData) data;
        if (!hudControlData.isVisible()) return;

        if (hudControlData.isScoreVisible()) {
            drawScores(game.scoreManager(), clock.tickCount(), nesColor(0x20), arcadeFontTS());
        }

        if (hudControlData.isLivesCounterVisible()) {
            drawLivesCounter(game, hudControlData, TS(2), sceneSize.y() - TS);
        }

        if (hudControlData.isLevelCounterVisible()) {
            float x = sceneSize.x() - TS(2), y = sceneSize.y() - TS;
            drawLevelCounter(game, hudControlData, x, y);
        }

        if (hudControlData.gameOptionsVisible()) {
            drawGameOptions(game.mapCategory(), game.difficulty(), game.pacBooster(), 0.5 * sceneSize.x(), TS(2.5));
        }
    }

    private void drawScores(ScoreManager scoreManager, long tick, Color color, Font font) {
        // show 1/2 second, hide 1/2 second
        if (tick % 60 < 30) {
            fillText("1UP", color, font, TS(4), TS(1));
        }
        fillText("HIGH SCORE", color, font, TS(11), TS(1));
        fillText("%6d".formatted(scoreManager.score().points()), color, font, TS(2), TS(2));
        fillText("%6d".formatted(scoreManager.highScore().points()), color, font, TS(13), TS(2));
    }

    private void drawLivesCounter(TengenMsPacMan_GameModel game, TengenMsPacMan_HUDControlData hudControlData, float x, float y) {
        RectShort sprite = spriteSheet().sprite(SpriteID.LIVES_COUNTER_SYMBOL);
        for (int i = 0; i < hudControlData.visibleLifeCount(); ++i) {
            drawSprite(sprite, x + TS(i * 2), y, true);
        }
        if (game.lifeCount() > game.maxLivesDisplayed()) {
            Font font = Font.font("Serif", FontWeight.BOLD, scaled(8));
            fillText("(%d)".formatted(game.lifeCount()), nesColor(0x28), font, x + TS(10), y + TS);
        }
    }

    private void drawLevelCounter(TengenMsPacMan_GameModel game, TengenMsPacMan_HUDControlData hudData, float x, float y) {
        GameLevel gameLevel = game.optGameLevel().orElse(null);
        if (gameLevel == null) return;

        if (hudData.levelNumberVisible()) {
            drawLevelNumberBox(gameLevel.number(), 0, y); // left box
            drawLevelNumberBox(gameLevel.number(), x, y); // right box
        }
        RectShort[] symbolSprites = spriteSheet().spriteSequence(SpriteID.BONUS_SYMBOLS);
        x -= TS(2);
        // symbols are drawn from right to left!
        for (byte symbol : game.levelCounterSymbols()) {
            if (0 <= symbol && symbol < symbolSprites.length) {
                drawSprite(symbolSprites[symbol], x, y, true);
            }
            x -= TS(2);
        }
    }

    public void drawGameOptions(MapCategory category, Difficulty difficulty, PacBooster booster, double centerX, double y) {
        RectShort categorySprite = switch (requireNonNull(category)) {
            case BIG     -> spriteSheet().sprite(SpriteID.INFO_CATEGORY_BIG);
            case MINI    -> spriteSheet().sprite(SpriteID.INFO_CATEGORY_MINI);
            case STRANGE -> spriteSheet().sprite(SpriteID.INFO_CATEGORY_STRANGE);
            case ARCADE  -> RectShort.ZERO;
        };
        RectShort difficultySprite = switch (requireNonNull(difficulty)) {
            case EASY   -> spriteSheet().sprite(SpriteID.INFO_DIFFICULTY_EASY);
            case HARD   -> spriteSheet().sprite(SpriteID.INFO_DIFFICULTY_HARD);
            case CRAZY  -> spriteSheet().sprite(SpriteID.INFO_DIFFICULTY_CRAZY);
            case NORMAL -> RectShort.ZERO;
        };
        drawSpriteCentered(centerX, y, spriteSheet().sprite(SpriteID.INFO_FRAME));
        if (requireNonNull(booster) != PacBooster.OFF) {
            drawSpriteCentered(centerX - TS(6), y, spriteSheet().sprite(SpriteID.INFO_BOOSTER));
        }
        drawSpriteCentered(centerX, y, difficultySprite);
        drawSpriteCentered(centerX + TS(4.5), y, categorySprite);
    }

    // this is also used by the 3D scene
    public void drawLevelNumberBox(int number, double x, double y) {
        drawSprite(spriteSheet().sprite(SpriteID.LEVEL_NUMBER_BOX), x, y, true);
        int tens = number / 10, ones = number % 10;
        if (tens > 0) {
            drawSprite(spriteSheet().digitSprite(tens), x + 2, y + 2, true);
        }
        drawSprite(spriteSheet().digitSprite(ones), x + 10, y + 2, true);
    }
}