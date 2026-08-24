/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.entities.LevelCounter;
import de.amr.pacmanfx.core.entities.LivesCounter;
import de.amr.pacmanfx.core.entities.Score;
import de.amr.pacmanfx.core.model.HUDState;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_WHITE;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_YELLOW;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_HeadsUpDisplayRenderer extends BaseRenderer implements SpriteRenderer, HeadsUpDisplay_Renderer {

    public static final String SCORE_TEXT = "SCORE";
    public static final String HIGH_SCORE_TEXT = "HIGH SCORE";

    private static final Color SCORE_TEXT_COLOR = ARCADE_WHITE;
    private static final Color SCORE_TEXT_COLOR_DISABLED = Color.GRAY;

    public ArcadeMsPacMan_HeadsUpDisplayRenderer(Canvas canvas) {
        super(canvas);
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return ArcadeMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void draw(GameSession session, GameScene gameScene, long tick) {
        requireNonNull(session);
        requireNonNull(gameScene);

        final HUDState hud = session.hud();

        if (!hud.isVisible()) return;

        if (hud.isScoreShown()) {
            drawScore(session.score(), SCORE_TEXT, arcadeFont8(), SCORE_TEXT_COLOR, tilesPx(1), tilesPx(1));

            final Score highScore = session.highScore();
            Color color = SCORE_TEXT_COLOR;
            if (!session.isAttractMode() && !highScore.data().isEnabled()) {
                color = SCORE_TEXT_COLOR_DISABLED;
            }
            drawScore(highScore, HIGH_SCORE_TEXT, arcadeFont8(), color, tilesPx(14), tilesPx(1));
        }

        if (hud.isLevelCounterShown()) {
            final LevelCounter levelCounter = session.hudEntities().theOne(LevelCounter.class);
            gameScene.optCanvasRendering().ifPresent(canvasRendering -> {
                final RectShort[] bonusSymbols = spriteSheet().findSpriteSequence(SpriteID.BONUS_SYMBOLS);
                final List<Integer> symbolCodes = levelCounter.data().symbolCodes();

                float x = canvasRendering.unscaledWidth() - tilesPx(4);
                final float y = canvasRendering.unscaledHeight() - tilesPx(2) + 2;
                for (int symbolCode : symbolCodes) {
                    drawSprite(bonusSymbols[symbolCode], x, y, true);
                    x -= tilesPx(2); // symbols are drawn from right to left
                }
            });
        }

        if (hud.isLivesCounterShown()) {
            final LivesCounter livesCounter = session.hudEntities().theOne(LivesCounter.class);
            final int count = livesCounter.data().numLives();
            gameScene.optCanvasRendering().ifPresent(canvasRendering -> {
                final RectShort sprite = spriteSheet().findSprite(SpriteID.LIVES_COUNTER_SYMBOL);
                final float x = tilesPx(2);
                final float y = canvasRendering.unscaledHeight() - tilesPx(2);
                for (int i = 0; i < count; ++i) {
                    drawSprite(sprite, x + i * tilesPx(2), y, true);
                }
                if (count > hud.maxLivesShown()) {
                    // show text indicating that more lives are available than symbols displayed (cheating may cause this)
                    Font hintFont = Font.font("Serif", FontWeight.BOLD, scaled(8));
                    fillText("%d".formatted(count), ARCADE_YELLOW, hintFont, x - 14, y + TS);
                }
            });
        }

        if (hud.isCreditShown()) {
            gameScene.optCanvasRendering().ifPresent(canvasRendering -> {
                final int credit = gameScene.game().coinMechanism().numCoins();
                fillText("CREDIT %2d".formatted(credit), ARCADE_WHITE, arcadeFont8(), tilesPx(2), canvasRendering.unscaledHeight());
            });
        }
    }

    private void drawScore(Score score, String title, Font font, Color color, double x, double y) {
        fillText(title, color, font, x, y);
        fillText("%7s".formatted("%02d".formatted(score.data().points())), color, font, x, y + TS + 1);
        if (score.data().points() != 0) {
            fillText("L" + score.data().levelNumber(), color, font, x + tilesPx(8), y + TS + 1);
        }
    }
}