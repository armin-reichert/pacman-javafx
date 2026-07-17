/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.HUDState;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.score.Score;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_CutScene1;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_CutScene2;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_CutScene3;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_CutScene4;
import de.amr.pacmanfx.tengenmspacman.model.Difficulty;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.model.PacBooster;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRendererMixin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static de.amr.pacmanfx.core.model.world.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.WorldMap.tilesPx;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_HeadsUpDisplay_Renderer extends BaseRenderer implements SpriteRendererMixin, HeadsUpDisplay_Renderer {

    private static final Color SCORE_TEXT_COLOR = NES_Palette.color(0x20);
    private static final Color SCORE_TEXT_COLOR_DISABLED = NES_Palette.color(0x10);

    public static final float LEVEL_COUNTER_POS_LEFT = tilesPx(2);
    public static final float LEVEL_COUNTER_POS_RIGHT = tilesPx(28);

    private final ObjectProperty<Font> totalLivesFont = new SimpleObjectProperty<>(Font.font("Serif", FontWeight.BOLD, 8));

    public TengenMsPacMan_HeadsUpDisplay_Renderer(Canvas canvas) {
        super(canvas);
        totalLivesFont.bind(scalingProperty().map(scaling -> Font.font("Serif", FontWeight.BOLD, scaling.doubleValue() * 8)));
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return TengenMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void draw(HUDState hud, GameContext gameContext, AbstractGameScene2D scene, long tick) {
        requireNonNull(hud);
        requireNonNull(gameContext);
        requireNonNull(scene);

        if (!hud.isVisible()) return;

        final TengenMsPacMan_GameModel model = (TengenMsPacMan_GameModel) gameContext.model();

        ctx.save();
        ctx.translate(0, scaled(computeOffsetY(scene)));

        if (hud.isScoreShown()) {
            // blink frequency = 1Hz (30 ticks on, 30 ticks off)
            final boolean on = tick % 60 < 30;
            drawScore(gameContext.model().score(), on, arcadeFont8());

            final Score highScore = gameContext.model().highScore();
            Color color = SCORE_TEXT_COLOR;
            if (!highScore.isEnabled() && !gameContext.gamePlay().isDemoLevelRunning(gameContext)) {
                color = SCORE_TEXT_COLOR_DISABLED;
            }
            drawHighScore(highScore, arcadeFont8(), color);
        }

        final int counterY = scene.unscaledHeight() - TS;

        if (hud.isLivesCounterShown()) {
            drawLivesCounter(gameContext, counterY);
        }

        gameContext.model().optLevel().ifPresent(level -> {
            if (hud.isLevelCounterShown()) {
                drawLevelCounter(level, hud, counterY);
            }
        });

        if (hud.gameOptionsVisible()) {
            drawGameOptions(model.mapCategory(), model.difficulty(), model.pacBoosterMode(), tilesPx(16), tilesPx(2.5f));
        }

        ctx.restore();
    }

    private double computeOffsetY(AbstractGameScene2D scene) {
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
            fillText("1UP", SCORE_TEXT_COLOR, font, tilesPx(4), tilesPx(1));
        }
        fillText("%6d".formatted(score.points()), SCORE_TEXT_COLOR, font, tilesPx(2), tilesPx(2));
    }

    private void drawHighScore(Score score, Font font, Color color) {
        fillText("HIGH SCORE", color, font, tilesPx(11), tilesPx(1));
        fillText("%6d".formatted(score.points()), color, font, tilesPx(13), tilesPx(2));
    }

    private void drawLivesCounter(GameContext gameContext, float y) {
        final RectShort symbolSprite = spriteSheet().findSprite(SpriteID.LIVES_COUNTER_SYMBOL);
        for (int i = 0; i < gameContext.hudState().visibleLifeCount(); ++i) {
            drawSprite(symbolSprite, tilesPx(4 + i * 2), y, true);
        }
        if (gameContext.model().lifeCount() > gameContext.hudState().maxLivesShown()) {
            fillText(
                "(%d)".formatted(gameContext.model().lifeCount()),
                NES_Palette.color(0x28),
                totalLivesFont.get(),
                tilesPx(14),
                y + TS);
        }
    }

    private void drawLevelCounter(GameLevel level, HUDState hud, float y) {
        final RectShort[] symbolSprites = spriteSheet().findSprites(SpriteID.BONUS_SYMBOLS);
        float x = LEVEL_COUNTER_POS_RIGHT - tilesPx(2);
        // symbols are drawn from right to left!
        for (int symbolCode : level.gameModel().levelCounter().symbolCodes()) {
            if (0 <= symbolCode && symbolCode < symbolSprites.length) {
                drawSprite(symbolSprites[symbolCode], x, y, true);
            }
            x -= tilesPx(2);
        }
        if (hud.isLevelNumberVisible()) {
            drawLevelNumberBox(level.number(), LEVEL_COUNTER_POS_LEFT, y); // left box
            drawLevelNumberBox(level.number(), LEVEL_COUNTER_POS_RIGHT, y); // right box
        }
    }

    // These methods are also used by the 3D scene, so make them public:

    public void drawLevelNumberBox(int number, double x, double y) {
        drawSprite(spriteSheet().findSprite(SpriteID.LEVEL_NUMBER_BOX), x, y, true);
        final int tens = number / 10, ones = number % 10;
        if (tens > 0) {
            drawSprite(spriteSheet().findDigitSprite(tens), x + 2, y + 2, true);
        }
        drawSprite(spriteSheet().findDigitSprite(ones), x + 10, y + 2, true);
    }

    public void drawGameOptions(MapCategory category, Difficulty difficulty, PacBooster booster, double centerX, double y) {
        final RectShort categorySprite = switch (category) {
            case BIG     -> spriteSheet().findSprite(SpriteID.INFO_CATEGORY_BIG);
            case MINI    -> spriteSheet().findSprite(SpriteID.INFO_CATEGORY_MINI);
            case STRANGE -> spriteSheet().findSprite(SpriteID.INFO_CATEGORY_STRANGE);
            case ARCADE  -> RectShort.NULL_RECTANGLE;
        };
        final RectShort difficultySprite = switch (difficulty) {
            case EASY   -> spriteSheet().findSprite(SpriteID.INFO_DIFFICULTY_EASY);
            case HARD   -> spriteSheet().findSprite(SpriteID.INFO_DIFFICULTY_HARD);
            case CRAZY  -> spriteSheet().findSprite(SpriteID.INFO_DIFFICULTY_CRAZY);
            case NORMAL -> RectShort.NULL_RECTANGLE;
        };
        drawSpriteCentered(spriteSheet().findSprite(SpriteID.INFO_FRAME), centerX, y);
        if (booster != PacBooster.OFF) {
            drawSpriteCentered(spriteSheet().findSprite(SpriteID.INFO_BOOSTER), centerX - tilesPx(5.5f), y);
        }
        drawSpriteCentered(difficultySprite, centerX, y);
        drawSpriteCentered(categorySprite, centerX + tilesPx(4.5f), y);
    }
}