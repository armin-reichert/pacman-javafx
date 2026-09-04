/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.*;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.MessageType;
import de.amr.pacmanfx.core.model.world.map.WorldMapConfigKey;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Extras;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_HUD_Options;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_CutScene1;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_CutScene2;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_CutScene3;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_CutScene4;
import de.amr.pacmanfx.tengenmspacman.model.BoosterMode;
import de.amr.pacmanfx.tengenmspacman.model.Difficulty;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.model.MessageAnimation;
import de.amr.pacmanfx.tengenmspacman.sprites.NES_WorldMapColorScheme;
import de.amr.pacmanfx.tengenmspacman.sprites.SpriteID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.SceneCanvasRenderingComp;
import de.amr.pacmanfx.ui.gamescene.d2.HUD_Renderer;
import de.amr.pacmanfx.ui.gamescene.d2.HUD_Style;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

import static de.amr.basics.math.Vector2f.vec2_float;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay.hasHUD_Option;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.GAME_OVER_TEXT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.READY_TEXT;
import static java.util.Objects.requireNonNull;

//TODO Use HUDStyle
public class TengenMsPacMan_HUD_Renderer
    extends BaseRenderer
    implements SpriteRenderer, HUD_Renderer {

    public static final float LEVEL_COUNTER_POS_LEFT = tilesPx(2);
    public static final float LEVEL_COUNTER_POS_RIGHT = tilesPx(28);

    private final ObjectProperty<Font> totalLivesFont = new SimpleObjectProperty<>(Font.font("Serif", FontWeight.BOLD, 8));

    private final HUD_Style style;

    public TengenMsPacMan_HUD_Renderer(HUD_Style style, Canvas canvas) {
        super(canvas);
        this.style = requireNonNull(style);

        totalLivesFont.bind(scalingProperty().map(scaling
            -> Font.font("Serif", FontWeight.BOLD, scaling.doubleValue() * 8)));
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
        final SceneCanvasRenderingComp canvasRendering = gameScene.reqCanvasRendering();

        if (!hud.isVisible()) return;

        ctx.save();
        ctx.translate(0, scaled(computeOffsetY(gameScene)));

        if (hasHUD_Option(session, TengenMsPacMan_HUD_Options.GAME_OPTIONS_VISIBLE)) {
            drawGameOptions(session, tilesPx(16), tilesPx(2.5f));
        }

        if (hud.gameScore().isVisible()) {
            drawScores(hud.gameScore(), hud.highScore(), session, tick);
        }

        final int lowerBorder = canvasRendering.unscaledHeight() - TS;

        if (hud.livesCounter().isVisible()) {
            drawLivesCounter(hud.livesCounter(), session, lowerBorder);
        }

        if (hud.levelCounter().isVisible()) {
            drawLevelCounter(hud.levelCounter(), session, lowerBorder);
        }

        ctx.restore();
    }

    @Override
    public void drawHUDEntity(GameEntity entity, GameSession session) {
        //TODO
    }

    @Override
    public void drawMessage(GameSession session) {

        session.optLevel().ifPresent(level -> {
            ctx.save();
            ctx.translate(scaled(TengenMsPacMan_PlayScene2D_Renderer.CONTENT_INDENT), 0);
            final MessageView messageView = session.hud().messageView();
            final Font scaledFont = Ufx.scaleFontBy(style.messageFont(), scaling());
            switch (messageView.data().messageType()) {
                case GAME_OVER -> drawGameOverMessage(session, level, scaledFont);
                case READY -> drawReadyMessage(level, scaledFont);
            }
            ctx.restore();
        });
    }

    private void drawGameOverMessage(GameSession session, GameLevel level, Font scaledFont) {
        final MessageAnimation animation = session.value(
            TengenMsPacMan_Extras.GAME_OVER_MESSAGE_ANIMATION, MessageAnimation.class);

        final Vector2f pos = animation != null
            ? animation.pos().asVector2f()
            : messagePosition(level);

        final NES_WorldMapColorScheme colorScheme = level.worldMap()
            .getConfigValue(WorldMapConfigKey.COLOR_SCHEME);

        final Color color = session.isAttractMode()
            ? Color.valueOf(colorScheme.wallStroke())
            : style.messageColor().apply(MessageType.GAME_OVER);

        fillTextCentered(GAME_OVER_TEXT, color, scaledFont, pos.x(), pos.y());
    }

    private void drawReadyMessage(GameLevel level, Font scaledFont) {
        final Vector2f pos = messagePosition(level);
        fillTextCentered(READY_TEXT,
            style.messageColor().apply(MessageType.READY),
            scaledFont,
            pos.x(), pos.y());
    }

    private Vector2f messagePosition(GameLevel level) {
        final House house = level.entities().house();
        final Vector2i houseSize = house.sizeInTiles();
        float cx = tilesPx(house.floorplan().minTile().x() + houseSize.x() * 0.5f);
        float cy = tilesPx(house.floorplan().minTile().y() + houseSize.y() + 1);
        return vec2_float(cx, cy);
    }

    private void drawScores(Score gameScore, Score highScore, GameSession session, long tick) {
        final Font scaledFont = Ufx.scaleFontBy(style.scoreTextFont(), scaling());

        // blink frequency = 1Hz (30 ticks on, 30 ticks off)
        final boolean on = tick % 60 < 30;
        if (on) {
            fillText(style.scoreText(), style.scoreTextColor(), scaledFont, tilesPx(4), tilesPx(1));
        }
        fillText("%6d".formatted(gameScore.data().points()), style.scoreTextColor(), scaledFont, tilesPx(2), tilesPx(2));

        Color color = style.scoreTextColor();
        if (!highScore.data().isEnabled() && !session.isAttractMode()) {
            color = style.scoreTextColorDisabled();
        }
        fillText("HIGH SCORE", color, scaledFont, tilesPx(11), tilesPx(1));
        fillText("%6d".formatted(highScore.data().points()), color, scaledFont, tilesPx(13), tilesPx(2));
    }

    private void drawLivesCounter(LivesCounter livesCounter, GameSession session, float y) {
        final int numLives = session.numLives();
        final int displayedSymbolsCount = Math.min(numLives - 1, livesCounter.data().maxLivesShown());

        final RectShort symbolSprite = style.livesCounterSymbolSprite();
        for (int i = 0; i < displayedSymbolsCount; ++i) {
            drawSprite(symbolSprite, tilesPx(4 + i * 2), y, true);
        }
        if (numLives - 1 > livesCounter.data().maxLivesShown()) {
            fillText("(%d)".formatted(numLives), NES_Palette.color(0x28), totalLivesFont.get(), tilesPx(14), y + TS);
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

    //TODO This does not belong here
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