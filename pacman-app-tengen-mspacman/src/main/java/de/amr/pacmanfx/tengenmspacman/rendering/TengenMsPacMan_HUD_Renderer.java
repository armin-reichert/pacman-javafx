/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.CreditDisplay;
import de.amr.pacmanfx.core.entities.LevelCounter;
import de.amr.pacmanfx.core.entities.LivesCounter;
import de.amr.pacmanfx.core.entities.Score;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.tengenmspacman.entities.GameOptionsDisplay;
import de.amr.pacmanfx.tengenmspacman.entities.LevelNumberDisplay;
import de.amr.pacmanfx.tengenmspacman.entities.gameoptionsdisplay.GameOptionsDataComp;
import de.amr.pacmanfx.tengenmspacman.model.BoosterMode;
import de.amr.pacmanfx.tengenmspacman.sprites.SpriteID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.uilib.entities.hud.comp.HUD_Style;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;

public class TengenMsPacMan_HUD_Renderer extends BaseRenderer implements SpriteRenderer {

    public TengenMsPacMan_HUD_Renderer(Canvas canvas) {
        super(canvas);
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return TengenMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void render(Renderable r, long tick) {
        if (!(r instanceof GameEntity entity)) {
            return;
        }

        if (!entity.isVisible()) {
            return;
        }

        switch (entity) {
            case LevelCounter levelCounter -> drawLevelCounter(levelCounter);
            case LivesCounter livesCounter -> drawLivesCounter(livesCounter);
            case Score score -> {
                switch (score.type()) {
                    case GAME_SCORE -> drawGameScore(score, tick);
                    case HIGH_SCORE -> drawHighScore(score);
                }
            }
            case GameOptionsDisplay gameOptionsDisplay -> drawGameOptionsDisplay(gameOptionsDisplay);
            case LevelNumberDisplay levelNumberDisplay -> drawLevelNumberDisplay(levelNumberDisplay);
            case CreditDisplay _ -> { /* Not used in this game variant */}

            default -> throw new IllegalStateException("Unexpected HUD entity: " + entity);
        }
    }

    private void drawGameOptionsDisplay(GameOptionsDisplay display) {
        final GameOptionsDataComp options = display.options();

        final RectShort mapCategorySprite = switch (options.mapCategory()) {
            case BIG     -> spriteSheet().findSprite(SpriteID.INFO_CATEGORY_BIG);
            case MINI    -> spriteSheet().findSprite(SpriteID.INFO_CATEGORY_MINI);
            case STRANGE -> spriteSheet().findSprite(SpriteID.INFO_CATEGORY_STRANGE);
            case ARCADE  -> null;
        };

        final RectShort difficultySprite = switch (options.difficulty()) {
            case EASY   -> spriteSheet().findSprite(SpriteID.INFO_DIFFICULTY_EASY);
            case HARD   -> spriteSheet().findSprite(SpriteID.INFO_DIFFICULTY_HARD);
            case CRAZY  -> spriteSheet().findSprite(SpriteID.INFO_DIFFICULTY_CRAZY);
            case NORMAL -> null;
        };

        final float centerX = display.pos().x();
        final float y = display.pos().y();

        drawSpriteCentered(spriteSheet().findSprite(SpriteID.INFO_FRAME), centerX, y);

        if (options.boosterMode() != BoosterMode.BOOSTER_OFF) {
            drawSpriteCentered(spriteSheet().findSprite(SpriteID.INFO_BOOSTER), centerX - tilesPx(5.5f), y);
        }
        if (difficultySprite != null) {
            drawSpriteCentered(difficultySprite, centerX, y);
        }
        if (mapCategorySprite != null) {
            drawSpriteCentered(mapCategorySprite, centerX + tilesPx(4.5f), y);
        }
    }

    private void drawGameScore(Score score, long tick) {
        final HUD_Style style = score.reqComp(HUD_Style.class);
        final Font scaledFont = Ufx.scaleFontBy(style.scoreTextFont(), scaling());
        // Blink frequency = 1Hz (30 ticks on, 30 ticks off)
        if (tick % 60 < 30) {
            fillText(style.scoreText(), style.scoreTextColor(), scaledFont, score.pos().x(), score.pos().y());
        }
        fillText("%6d".formatted(score.data().points()),
            style.scoreTextColor(), scaledFont, 2 * TS, score.pos().y() + TS);
    }

    private void drawHighScore(Score score) {
        final HUD_Style style = score.reqComp(HUD_Style.class);
        final Font scaledFont = Ufx.scaleFontBy(style.scoreTextFont(), scaling());
        final Color color = score.data().isEnabled() ? style.scoreTextColor(): style.scoreTextColorDisabled();
        fillText("HIGH SCORE", color, scaledFont, score.pos().x(), score.pos().y());
        fillText("%6d".formatted(score.data().points()), color, scaledFont,
            score.pos().x() + 2 * TS, score.pos().y() + TS
        );
    }

    private void drawLivesCounter(LivesCounter livesCounter) {
        final HUD_Style style = livesCounter.reqComp(HUD_Style.class);
        final float x = livesCounter.pos().x();
        final float y = livesCounter.pos().y();

        final int numLives      = livesCounter.data().numLives();
        final int numLivesShown = livesCounter.data().numLivesShown();
        final int maxLivesShown = livesCounter.data().maxLivesShown();

        for (int i = 0; i < numLivesShown; ++i) {
            drawSprite(style.livesCounterSymbolSprite(), x + i * tilesPx(2), y, true);
        }

        if (numLives > maxLivesShown) {
            final Font scaledFont = Font.font("Serif", FontWeight.BLACK, scaled(8));
            fillText("(%d)".formatted(numLives), NES_Palette.color(0x28), scaledFont, tilesPx(14), y + TS);
        }
    }

    private void drawLevelCounter(LevelCounter levelCounter) {
        float x = levelCounter.pos().x();
        float y = levelCounter.pos().y();

        // Symbols are drawn from right to left!
        final RectShort[] symbolSprites = spriteSheet().findSpriteSequence(SpriteID.BONUS_SYMBOLS);
        for (int code : levelCounter.data().symbolCodes()) {
            if (0 <= code && code < symbolSprites.length) {
                drawSprite(symbolSprites[code], x, y, true);
            }
            x -= tilesPx(2);
        }
    }

    private void drawLevelNumberDisplay(LevelNumberDisplay display) {
        final float x = display.pos().x();
        final float y = display.pos().y();
        final int number = display.levelNumber().number();

        drawSprite(spriteSheet().findSprite(SpriteID.LEVEL_NUMBER_BOX), x, y, true);

        final int tens = number / 10;
        if (tens > 0) {
            final RectShort tensSprite = spriteSheet().findDigitSprite(number / 10);
            drawSprite(tensSprite, x + 2, y + 2, true);
        }

        final RectShort onesSprite = spriteSheet().findDigitSprite(number % 10);
        drawSprite(onesSprite, x + 10, y + 2, true);
    }
}