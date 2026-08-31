/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.entities.LevelCounter;
import de.amr.pacmanfx.core.entities.LivesCounter;
import de.amr.pacmanfx.core.entities.Score;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_HUD_Options;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_CutScene1;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_CutScene2;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_CutScene3;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_CutScene4;
import de.amr.pacmanfx.tengenmspacman.model.BoosterMode;
import de.amr.pacmanfx.tengenmspacman.model.Difficulty;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.sprites.SpriteID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
import de.amr.pacmanfx.ui.gamescene.d2.HUD_Renderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay.hasHUD_Option;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_HUD_Renderer
    extends BaseRenderer
    implements SpriteRenderer, HUD_Renderer {

    private static final Color SCORE_TEXT_COLOR = NES_Palette.color(0x20);
    private static final Color SCORE_TEXT_COLOR_DISABLED = NES_Palette.color(0x10);

    public static final float LEVEL_COUNTER_POS_LEFT = tilesPx(2);
    public static final float LEVEL_COUNTER_POS_RIGHT = tilesPx(28);

    private final ObjectProperty<Font> totalLivesFont = new SimpleObjectProperty<>(Font.font("Serif", FontWeight.BOLD, 8));

    public TengenMsPacMan_HUD_Renderer(Canvas canvas) {
        super(canvas);
        totalLivesFont.bind(scalingProperty().map(scaling -> Font.font("Serif", FontWeight.BOLD, scaling.doubleValue() * 8)));
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return TengenMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void drawHUD(HUD hud, GameSession session, GameScene gameScene, long tick) {
        requireNonNull(hud);
        requireNonNull(session);
        requireNonNull(gameScene);

        if (gameScene.optCanvasRendering().isEmpty()) {
            return; // Should not happen, but...
        }
        final CanvasRenderingComp canvasRendering = gameScene.reqCanvasRendering();

        if (!hud.isVisible()) return;

        ctx.save();
        ctx.translate(0, scaled(computeOffsetY(gameScene)));

        if (hasHUD_Option(session, TengenMsPacMan_HUD_Options.GAME_OPTIONS_VISIBLE)) {
            drawGameOptions(session, tilesPx(16), tilesPx(2.5f));
        }

        if (hud.gameScore().isVisible()) {
            drawScores(hud.gameScore(), hud.highScore(), session, session.thisFrame().tick());
        }

        final int counterY = canvasRendering.unscaledHeight() - TS;

        if (hud.livesCounter().isVisible()) {
            drawLivesCounter(hud.livesCounter(), session, counterY);
        }

        if (hud.levelCounter().isVisible()) {
            drawLevelCounter(hud.levelCounter(), session, counterY);
        }

        ctx.restore();
    }

    private void drawScores(Score gameScore, Score highScore, GameSession session, long tick) {
        // blink frequency = 1Hz (30 ticks on, 30 ticks off)
        final boolean on = tick % 60 < 30;
        drawScore(gameScore, on, arcadeFont8());

        Color color = SCORE_TEXT_COLOR;
        if (!highScore.data().isEnabled() && !session.isAttractMode()) {
            color = SCORE_TEXT_COLOR_DISABLED;
        }
        drawHighScore(highScore, arcadeFont8(), color);
    }

    private void drawScore(Score score, boolean on, Font font) {
        if (on) {
            fillText("1UP", SCORE_TEXT_COLOR, font, tilesPx(4), tilesPx(1));
        }
        fillText("%6d".formatted(score.data().points()), SCORE_TEXT_COLOR, font, tilesPx(2), tilesPx(2));
    }

    private void drawHighScore(Score score, Font font, Color color) {
        fillText("HIGH SCORE", color, font, tilesPx(11), tilesPx(1));
        fillText("%6d".formatted(score.data().points()), color, font, tilesPx(13), tilesPx(2));
    }

    private void drawLivesCounter(LivesCounter livesCounter, GameSession session, float y) {
        final int numLives = session.numLives();
        final int displayedSymbolsCount = Math.min(numLives - 1, livesCounter.data().maxLivesShown());

        final RectShort symbolSprite = spriteSheet().findSprite(SpriteID.LIVES_COUNTER_SYMBOL);
        for (int i = 0; i < displayedSymbolsCount; ++i) {
            drawSprite(symbolSprite, tilesPx(4 + i * 2), y, true);
        }
        if (numLives - 1 > livesCounter.data().maxLivesShown()) {
            fillText("(%d)".formatted(numLives), NES_Palette.color(0x28), totalLivesFont.get(),
                tilesPx(14), y + TS);
        }
    }

    private void drawLevelCounter(LevelCounter levelCounter, GameSession session, float y) {
        final RectShort[] symbolSprites = spriteSheet().findSpriteSequence(SpriteID.BONUS_SYMBOLS);
        float x = LEVEL_COUNTER_POS_RIGHT - tilesPx(2);
        // symbols are drawn from right to left!
        final List<Integer> symbolCodes = levelCounter.data().symbolCodes();
        for (int code : symbolCodes) {
            if (0 <= code && code < symbolSprites.length) {
                drawSprite(symbolSprites[code], x, y, true);
            }
            x -= tilesPx(2);
        }
        if (hasHUD_Option(session, TengenMsPacMan_HUD_Options.LEVEL_NUMBER_VISIBLE)) {
            session.optLevel().ifPresent(level -> {
                final int number = level.number();
                drawLevelNumberBox(number, LEVEL_COUNTER_POS_LEFT, y);
                drawLevelNumberBox(number, LEVEL_COUNTER_POS_RIGHT, y);
            });
        }
    }

    private double computeOffsetY(GameScene scene) {
        return switch (scene) {
            case TengenMsPacMan_CutScene1 _,
                 TengenMsPacMan_CutScene2 _,
                 TengenMsPacMan_CutScene3 _,
                 TengenMsPacMan_CutScene4 _ -> -2 * TS;
            default -> 0;
        };
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

    public void drawGameOptions(GameSession session, double centerX, double y) {
        final MapCategory mapCategory = TengenMsPacMan_GamePlay.mapCategory(session);
        final Difficulty difficulty   = TengenMsPacMan_GamePlay.difficulty(session);
        final BoosterMode boosterMode = TengenMsPacMan_GamePlay.boosterMode(session);

        final RectShort mapCategorySprite = switch (mapCategory) {
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
        if (boosterMode != BoosterMode.BOOSTER_OFF) {
            drawSpriteCentered(spriteSheet().findSprite(SpriteID.INFO_BOOSTER), centerX - tilesPx(5.5f), y);
        }
        drawSpriteCentered(difficultySprite, centerX, y);
        drawSpriteCentered(mapCategorySprite, centerX + tilesPx(4.5f), y);
    }
}