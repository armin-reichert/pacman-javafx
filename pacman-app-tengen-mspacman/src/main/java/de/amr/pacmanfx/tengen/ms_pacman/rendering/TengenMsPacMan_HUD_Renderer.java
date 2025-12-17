/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.HUD;
import de.amr.pacmanfx.model.Score;
import de.amr.pacmanfx.tengen.ms_pacman.model.*;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.HUD_Renderer;
import de.amr.pacmanfx.uilib.GameClock;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesColor;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_HUD_Renderer extends BaseRenderer implements SpriteRenderer, HUD_Renderer {

    private static final Color SCORE_TEXT_COLOR = nesColor(0x20);
    private static final Color SCORE_TEXT_COLOR_DISABLED = nesColor(0x16);

    public static final float LEVEL_COUNTER_POS_LEFT = TS(2);
    public static final float LEVEL_COUNTER_POS_RIGHT = TS(28);

    private final GameClock clock;
    private final TengenMsPacMan_SpriteSheet spriteSheet;

    private final ObjectProperty<Font> totalLivesFont = new SimpleObjectProperty<>(Font.font("Serif", FontWeight.BOLD, 8));

    private float offsetY = 0;

    public TengenMsPacMan_HUD_Renderer(Canvas canvas, TengenMsPacMan_SpriteSheet spriteSheet, GameClock clock) {
        super(canvas);
        this.clock = requireNonNull(clock);
        this.spriteSheet = requireNonNull(spriteSheet);
        totalLivesFont.bind(scalingProperty().map(scaling -> Font.font("Serif", FontWeight.BOLD, scaling.doubleValue() * 8)));
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public void draw(HUD hud, Game game, GameScene2D scene) {
        requireNonNull(game);
        requireNonNull(scene);

        final Vector2i sceneSize = scene.unscaledSize();
        final var tengenGame = (TengenMsPacMan_GameModel) game;
        final TengenMsPacMan_HUD tengenHUD = tengenGame.hud();

        if (!hud.isVisible()) return;

        ctx.save();
        ctx.translate(0, scaled(offsetY));

        if (hud.isScoreVisible()) {
            drawScore(game.score(), clock.tickCount(), arcadeFont8());

            final Score highScore = game.highScore();
            Color color = SCORE_TEXT_COLOR;
            if (!highScore.isEnabled() && !game.level().isDemoLevel()) {
                color = SCORE_TEXT_COLOR_DISABLED;
            }
            drawHighScore(highScore, arcadeFont8(), color);
        }

        if (hud.isLivesCounterVisible()) {
            drawLivesCounter(tengenGame, tengenHUD, sceneSize.y() - TS);
        }

        if (hud.isLevelCounterVisible() && game.optGameLevel().isPresent()) {
            drawLevelCounter(game.level(), tengenHUD, sceneSize.y() - TS);
        }

        if (tengenHUD.gameOptionsVisible()) {
            drawGameOptions(tengenGame.mapCategory(), tengenGame.difficulty(), tengenGame.pacBooster(), TS(16), TS(2.5f));
        }

        ctx.restore();
    }

    private void drawScore(Score score, long tick, Font font) {
        // blink frequency=1Hz (30 ticks on, 30 ticks off)
        final boolean on = tick % 60 < 30;
        if (on) {
            fillText("1UP", SCORE_TEXT_COLOR, font, TS(4), TS(1));
        }
        fillText("%6d".formatted(score.points()), SCORE_TEXT_COLOR, font, TS(2), TS(2));
    }

    private void drawHighScore(Score score, Font font, Color color) {
        fillText("HIGH SCORE", color, font, TS(11), TS(1));
        fillText("%6d".formatted(score.points()), color, font, TS(13), TS(2));
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

    private void drawLevelCounter(GameLevel level, TengenMsPacMan_HUD hud, float y) {
        final RectShort[] symbolSprites = spriteSheet().spriteSequence(SpriteID.BONUS_SYMBOLS);
        float x = LEVEL_COUNTER_POS_RIGHT - TS(2);
        // symbols are drawn from right to left!
        for (byte symbol : level.game().levelCounter().levelCounterSymbols()) {
            if (0 <= symbol && symbol < symbolSprites.length) {
                drawSprite(symbolSprites[symbol], x, y, true);
            }
            x -= TS(2);
        }
        if (hud.levelNumberVisible()) {
            drawLevelNumberBox(level.number(), LEVEL_COUNTER_POS_LEFT, y); // left box
            drawLevelNumberBox(level.number(), LEVEL_COUNTER_POS_RIGHT, y); // right box
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