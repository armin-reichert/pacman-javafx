/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.HeadsUpDisplay;
import de.amr.pacmanfx.model.Score;
import de.amr.pacmanfx.tengenmspacman.model.*;
import de.amr.pacmanfx.tengenmspacman.scenes.TengenMsPacMan_CutScene1;
import de.amr.pacmanfx.tengenmspacman.scenes.TengenMsPacMan_CutScene2;
import de.amr.pacmanfx.tengenmspacman.scenes.TengenMsPacMan_CutScene3;
import de.amr.pacmanfx.tengenmspacman.scenes.TengenMsPacMan_CutScene4;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.nesColor;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_HeadsUpDisplay_Renderer extends BaseRenderer implements SpriteRenderer, HeadsUpDisplay_Renderer {

    private static final Color SCORE_TEXT_COLOR = nesColor(0x20);
    private static final Color SCORE_TEXT_COLOR_DISABLED = nesColor(0x16);

    public static final float LEVEL_COUNTER_POS_LEFT = TS(2);
    public static final float LEVEL_COUNTER_POS_RIGHT = TS(28);

    private final ObjectProperty<Font> totalLivesFont = new SimpleObjectProperty<>(Font.font("Serif", FontWeight.BOLD, 8));

    public TengenMsPacMan_HeadsUpDisplay_Renderer(Canvas canvas) {
        super(canvas);
        totalLivesFont.bind(scalingProperty().map(scaling -> Font.font("Serif", FontWeight.BOLD, scaling.doubleValue() * 8)));
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return TengenMsPacMan_SpriteSheet.INSTANCE;
    }

    @Override
    public void draw(HeadsUpDisplay hud, Game game, GameScene2D scene) {
        requireNonNull(hud);
        requireNonNull(game);
        requireNonNull(scene);

        if (!hud.isVisible()) return;
        if (!(hud instanceof TengenMsPacMan_HeadsUpDisplay tengenHUD)) return;
        if (!(game instanceof TengenMsPacMan_GameModel tengenGame)) return;

        ctx.save();
        ctx.translate(0, scaled(computeOffsetY(scene)));

        if (hud.isScoreVisible()) {
            // blink frequency = 1Hz (30 ticks on, 30 ticks off)
            final boolean on = scene.ui().clock().tickCount() % 60 < 30;
            drawScore(game.score(), on, arcadeFont8());

            final Score highScore = game.highScore();
            Color color = SCORE_TEXT_COLOR;
            if (!highScore.isEnabled() && !game.level().isDemoLevel()) {
                color = SCORE_TEXT_COLOR_DISABLED;
            }
            drawHighScore(highScore, arcadeFont8(), color);
        }

        final int counterY = scene.unscaledSize().y() - TS;

        if (hud.isLivesCounterVisible()) {
            drawLivesCounter(tengenGame, tengenHUD, counterY);
        }

        game.optGameLevel().ifPresent(level -> {
            if (tengenHUD.isLevelCounterVisible()) {
                drawLevelCounter(level, tengenHUD, counterY);
            }
        });

        if (tengenHUD.gameOptionsVisible()) {
            drawGameOptions(tengenGame.mapCategory(), tengenGame.difficulty(), tengenGame.pacBooster(), TS(16), TS(2.5f));
        }

        ctx.restore();
    }

    private double computeOffsetY(GameScene2D scene) {
        return switch (scene) {
            case TengenMsPacMan_CutScene1 ignored -> -2 * TS;
            case TengenMsPacMan_CutScene2 ignored -> -2 * TS;
            case TengenMsPacMan_CutScene3 ignored -> -2 * TS;
            case TengenMsPacMan_CutScene4 ignored -> -2 * TS;
            default -> 0;
        };
    }

    private void drawScore(Score score, boolean on, Font font) {
        if (on) {
            fillText("1UP", SCORE_TEXT_COLOR, font, TS(4), TS(1));
        }
        fillText("%6d".formatted(score.points()), SCORE_TEXT_COLOR, font, TS(2), TS(2));
    }

    private void drawHighScore(Score score, Font font, Color color) {
        fillText("HIGH SCORE", color, font, TS(11), TS(1));
        fillText("%6d".formatted(score.points()), color, font, TS(13), TS(2));
    }

    private void drawLivesCounter(Game game, TengenMsPacMan_HeadsUpDisplay hud, float y) {
        final RectShort symbolSprite = spriteSheet().sprite(SpriteID.LIVES_COUNTER_SYMBOL);
        for (int i = 0; i < hud.visibleLifeCount(); ++i) {
            drawSprite(symbolSprite, TS(4 + i * 2), y, true);
        }
        if (game.lifeCount() > game.hud().maxLivesDisplayed()) {
            fillText("(%d)".formatted(game.lifeCount()), nesColor(0x28), totalLivesFont.get(), TS(14), y + TS);
        }
    }

    private void drawLevelCounter(GameLevel level, TengenMsPacMan_HeadsUpDisplay hud, float y) {
        final RectShort[] symbolSprites = spriteSheet().sprites(SpriteID.BONUS_SYMBOLS);
        float x = LEVEL_COUNTER_POS_RIGHT - TS(2);
        // symbols are drawn from right to left!
        for (byte symbol : level.game().levelCounterSymbols()) {
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

    // These methods are also used by the 3D scene, so make them public:

    public void drawLevelNumberBox(int number, double x, double y) {
        drawSprite(spriteSheet().sprite(SpriteID.LEVEL_NUMBER_BOX), x, y, true);
        final int tens = number / 10, ones = number % 10;
        if (tens > 0) {
            drawSprite(spriteSheet().digitSprite(tens), x + 2, y + 2, true);
        }
        drawSprite(spriteSheet().digitSprite(ones), x + 10, y + 2, true);
    }

    public void drawGameOptions(MapCategory category, Difficulty difficulty, PacBooster booster, double centerX, double y) {
        final RectShort categorySprite = switch (category) {
            case BIG     -> spriteSheet().sprite(SpriteID.INFO_CATEGORY_BIG);
            case MINI    -> spriteSheet().sprite(SpriteID.INFO_CATEGORY_MINI);
            case STRANGE -> spriteSheet().sprite(SpriteID.INFO_CATEGORY_STRANGE);
            case ARCADE  -> RectShort.ZERO;
        };
        final RectShort difficultySprite = switch (difficulty) {
            case EASY   -> spriteSheet().sprite(SpriteID.INFO_DIFFICULTY_EASY);
            case HARD   -> spriteSheet().sprite(SpriteID.INFO_DIFFICULTY_HARD);
            case CRAZY  -> spriteSheet().sprite(SpriteID.INFO_DIFFICULTY_CRAZY);
            case NORMAL -> RectShort.ZERO;
        };
        drawSpriteCentered(centerX, y, spriteSheet().sprite(SpriteID.INFO_FRAME));
        if (booster != PacBooster.OFF) {
            drawSpriteCentered(centerX - TS(5.5f), y, spriteSheet().sprite(SpriteID.INFO_BOOSTER));
        }
        drawSpriteCentered(centerX, y, difficultySprite);
        drawSpriteCentered(centerX + TS(4.5f), y, categorySprite);
    }
}