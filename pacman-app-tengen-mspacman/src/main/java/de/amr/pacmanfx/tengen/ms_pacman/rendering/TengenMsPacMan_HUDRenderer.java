/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.HUD;
import de.amr.pacmanfx.model.ScoreManager;
import de.amr.pacmanfx.tengen.ms_pacman.model.*;
import de.amr.pacmanfx.uilib.GameClock;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.BaseSpriteRenderer;
import de.amr.pacmanfx.uilib.rendering.HUDRenderer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesColor;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_HUDRenderer extends BaseSpriteRenderer implements HUDRenderer {

    private final GameClock clock;

    private final ObjectProperty<Font> totalLivesFont = new SimpleObjectProperty<>(Font.font("Serif", FontWeight.BOLD, 8));

    public TengenMsPacMan_HUDRenderer(Canvas canvas, SpriteSheet<?> spriteSheet, GameClock clock) {
        super(canvas, spriteSheet);
        this.clock = requireNonNull(clock);
        totalLivesFont.bind(scalingProperty().map(scaling -> Font.font("Serif", FontWeight.BOLD, scaling.doubleValue() * 8)));
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return (TengenMsPacMan_SpriteSheet) super.spriteSheet();
    }

    @Override
    public void drawHUD(Game game, HUD hud, Vector2i sceneSize) {
        requireNonNull(game);
        requireNonNull(hud);
        requireNonNull(sceneSize);

        TengenMsPacMan_GameModel tengenGame = (TengenMsPacMan_GameModel) game;
        TengenMsPacMan_HUD tengenHUD = (TengenMsPacMan_HUD) hud;

        if (!tengenHUD.isVisible()) return;

        if (tengenHUD.isScoreVisible()) {
            drawScores(tengenGame.scoreManager(), clock.tickCount(), nesColor(0x20), arcadeFont8());
        }

        if (tengenHUD.isLivesCounterVisible()) {
            drawLivesCounter(tengenGame, tengenHUD, sceneSize.y() - TS);
        }

        if (tengenHUD.isLevelCounterVisible()) {
            float y = sceneSize.y() - TS;
            drawLevelCounter(tengenGame, tengenHUD, y);
        }

        if (tengenHUD.gameOptionsVisible()) {
            drawGameOptions(tengenGame.mapCategory(), tengenGame.difficulty(), tengenGame.pacBooster(), TS(16), TS(2.5f));
        }
    }

    private void drawScores(ScoreManager scoreManager, long tick, Color color, Font font) {
        if (tick % 60 < 30) { // show for 0.5 seconds, hide for 0.5 seconds
            fillText("1UP", color, font, TS(4), TS(1));
        }
        fillText("HIGH SCORE", color, font, TS(11), TS(1));
        fillText("%6d".formatted(scoreManager.score().points()), color, font, TS(2), TS(2));
        fillText("%6d".formatted(scoreManager.highScore().points()), color, font, TS(13), TS(2));
    }

    private void drawLivesCounter(Game game, TengenMsPacMan_HUD hud, float y) {
        RectShort sprite = spriteSheet().sprite(SpriteID.LIVES_COUNTER_SYMBOL);
        for (int i = 0; i < hud.visibleLifeCount(); ++i) {
            drawSprite(sprite, TS(4 + i * 2), y, true);
        }
        if (game.lifeCount() > game.hud().maxLivesDisplayed()) {
            fillText("(%d)".formatted(game.lifeCount()), nesColor(0x28), totalLivesFont.get(), TS(14), y + TS);
        }
    }

    private void drawLevelCounter(Game game, TengenMsPacMan_HUD hud, float y) {
        GameLevel gameLevel = game.optGameLevel().orElse(null);
        if (gameLevel == null) return;

        float left = TS(2), right = TS(28);
        if (hud.levelNumberVisible()) {
            drawLevelNumberBox(gameLevel.number(), left, y); // left box
            drawLevelNumberBox(gameLevel.number(), right, y); // right box
        }
        RectShort[] symbolSprites = spriteSheet().spriteSequence(SpriteID.BONUS_SYMBOLS);
        right -= TS(2);
        // symbols are drawn from right to left!
        for (byte symbol : game.levelCounter().symbols()) {
            if (0 <= symbol && symbol < symbolSprites.length) {
                drawSprite(symbolSprites[symbol], right, y, true);
            }
            right -= TS(2);
        }
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
            drawSpriteCentered(centerX - TS(5.5f), y, spriteSheet().sprite(SpriteID.INFO_BOOSTER));
        }
        drawSpriteCentered(centerX, y, difficultySprite);
        drawSpriteCentered(centerX + TS(4.5f), y, categorySprite);
    }
}