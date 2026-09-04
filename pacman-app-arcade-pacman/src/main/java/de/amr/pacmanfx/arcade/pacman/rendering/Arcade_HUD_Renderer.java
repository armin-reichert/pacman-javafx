/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.*;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.MessageType;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.HUD_Renderer;
import de.amr.pacmanfx.ui.gamescene.d2.HUD_Style;
import de.amr.pacmanfx.ui.gamescene.d2.SceneCanvasRenderingComp;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static de.amr.basics.math.Vector2f.vec2_float;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.*;
import static java.util.Objects.requireNonNull;

public class Arcade_HUD_Renderer extends BaseRenderer implements SpriteRenderer, HUD_Renderer {

    public static final String GAME_OVER_TEXT = "GAME  OVER";
    public static final String READY_TEXT = "READY!";

    protected final HUD_Style style;

    public Arcade_HUD_Renderer(HUD_Style style, Canvas canvas) {
        super(canvas);
        this.style = requireNonNull(style);
    }

    @Override
    public SpriteSheet spriteSheet() {
        return style.spriteSheet();
    }

    @Override
    public void drawHUD(HUD hud, GameSession session, GameScene gameScene, long tick) {
        requireNonNull(hud);
        requireNonNull(session);
        requireNonNull(gameScene);

        if (gameScene.optCanvasRendering().isEmpty()) {
            return;
        }
        final SceneCanvasRenderingComp sceneCanvasRendering = gameScene.reqCanvasRendering();

        if (!hud.isVisible()) return;

        final Font scaledFont = Ufx.scaleFontBy(style.scoreTextFont(), scaling());

        /*
        if (hud.gameScore().isVisible()) {
            final boolean highScoreDisabled = session.isAttractMode() || !session.hud().highScore().data().isEnabled();
            final Color highScoreTextColor = highScoreDisabled ? style.scoreTextColorDisabled() : style.scoreTextColor();
            drawScore(hud.gameScore(), style.scoreText(), scaledFont, style.scoreTextColor(), tilesPx(1), tilesPx(1));
            drawScore(hud.highScore(), style.highScoreText(), scaledFont, highScoreTextColor, tilesPx(14), tilesPx(1));
        }

        if (hud.levelCounter().isVisible()) {
            drawLevelCounter(hud.levelCounter());
        }

        if (hud.livesCounter().isVisible()) {
            drawLivesCounter(hud.livesCounter(), session);
        }

         */

        if (hud.isCreditVisible()) {
            final String text = style.creditTextFormat().formatted(gameScene.game().coinMechanism().numCoins());
            fillText(text, ARCADE_WHITE, scaledFont, tilesPx(2), sceneCanvasRendering.unscaledHeight());
        }
    }

    @Override
    public void drawHUDEntity(GameEntity entity, GameSession session) {
        if (!entity.isVisible()) return;
        switch (entity) {
            case MessageView messageView -> drawMessage(messageView, session);
            case LevelCounter levelCounter -> drawLevelCounter(levelCounter);
            case LivesCounter livesCounter -> drawLivesCounter(livesCounter, session);
            case Score score -> {
                final Font scaledFont = Ufx.scaleFontBy(style.scoreTextFont(), scaling());
                if (score.type() == Score.Type.GAME_SCORE) {
                    drawScoreText(score, style.scoreText(), scaledFont, style.scoreTextColor());
                } else {
                    final boolean highScoreDisabled = session.isAttractMode() || !session.hud().highScore().data().isEnabled();
                    final Color highScoreTextColor = highScoreDisabled ? style.scoreTextColorDisabled() : style.scoreTextColor();
                    drawScoreText(score, style.highScoreText(), scaledFont, highScoreTextColor);
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + entity);
        }
    }

    @Override
    public void drawMessage(GameSession session) {
        drawMessage(session.hud().messageView(), session);
    }

    private void drawMessage(MessageView messageView, GameSession session) {
        if (messageView.data().messageType() != MessageType.NO_MESSAGE) {
            final Vector2f pos = messagePosition(session.level());
            final Font scaledFont = Ufx.scaleFontBy(style.messageFont(), scaling());
            switch (messageView.data().messageType()) {
                case GAME_OVER -> fillTextCentered(GAME_OVER_TEXT, ARCADE_RED, scaledFont, pos.x(), pos.y());
                case READY -> fillTextCentered(READY_TEXT, ARCADE_YELLOW, scaledFont, pos.x(), pos.y());
            }
        }
    }

    private void drawScoreText(Score score, String title, Font font, Color color) {
        final float x = score.pos().x();
        final float y = score.pos().y();
        fillText(title, color, font, x, y);
        fillText("%7s".formatted("%02d".formatted(score.data().points())), color, font, x, y + TS + 1);
        if (score.data().points() != 0) {
            fillText("L" + score.data().levelNumber(), color, font, x + tilesPx(8), y + TS + 1);
        }
    }

    private void drawLivesCounter(LivesCounter livesCounter, GameSession session) {
        final int numLives = session.numLives();
        final int displayedSymbolsCount = Math.min(numLives - 1, livesCounter.data().maxLivesShown());

        final float x = livesCounter.pos().x();
        final float y = livesCounter.pos().y();

        final float spacing = tilesPx(2);
        // Draw at most (numLives - 1) symbols in lives counter
        for (int i = 0; i < displayedSymbolsCount; ++i) {
            drawSprite(style.livesCounterSymbolSprite(), x + i * spacing, y, true);
        }
        if (numLives - 1 > livesCounter.data().maxLivesShown()) {
            // Show text indicating that more lives are available than symbols displayed (cheating may cause this)
            final Font font = Font.font("Serif", FontWeight.BOLD, scaled(8));
            fillText("%d".formatted(numLives), ARCADE_YELLOW, font, x - 14, y + TS);
        }
    }

    private void drawLevelCounter(LevelCounter levelCounter) {
        float x = levelCounter.pos().x();
        final float y = levelCounter.pos().y();
        for (int symbolCode : levelCounter.data().symbolCodes()) {
            drawSprite(style.bonusSymbolSprites()[symbolCode], x, y, true);
            x -= tilesPx(2); // symbols are drawn from right to left
        }
    }

    private Vector2f messagePosition(GameLevel level) {
        final House house = level.entities().house();
        Vector2i houseSize = house.sizeInTiles();
        float cx = tilesPx(house.floorplan().minTile().x() + houseSize.x() * 0.5f);
        float cy = tilesPx(house.floorplan().minTile().y() + houseSize.y() + 1);
        return vec2_float(cx, cy);
    }
}
