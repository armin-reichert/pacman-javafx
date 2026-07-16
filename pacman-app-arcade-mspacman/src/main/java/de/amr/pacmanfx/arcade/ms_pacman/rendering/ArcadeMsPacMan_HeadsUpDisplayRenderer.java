/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.HUDState;
import de.amr.pacmanfx.core.score.Score;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRendererMixin;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static de.amr.pacmanfx.core.model.world.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.WorldMap.tilesPx;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_WHITE;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_YELLOW;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_HeadsUpDisplayRenderer extends BaseRenderer implements SpriteRendererMixin, HeadsUpDisplay_Renderer {

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
    public void draw(HUDState hud, GameContext gameContext, AbstractGameScene2D scene, long tick) {
        requireNonNull(gameContext);
        requireNonNull(scene);

        if (!hud.isVisible()) return;

        if (hud.isScoreShown()) {
            drawScore(gameContext.model().score(), SCORE_TEXT, arcadeFont8(), SCORE_TEXT_COLOR, tilesPx(1), tilesPx(1));

            final Score highScore = gameContext.model().highScore();
            Color color = SCORE_TEXT_COLOR;
            if (!gameContext.gamePlay().isDemoLevelRunning(gameContext) && !highScore.isEnabled()) {
                color = SCORE_TEXT_COLOR_DISABLED;
            }
            drawScore(highScore, HIGH_SCORE_TEXT, arcadeFont8(), color, tilesPx(14), tilesPx(1));
        }

        if (hud.isLevelCounterShown()) {
            final RectShort[] bonusSymbols = spriteSheet().sprites(SpriteID.BONUS_SYMBOLS);
            float x = scene.unscaledWidth() - tilesPx(4);
            final float y = scene.unscaledHeight() - tilesPx(2) + 2;
            for (int symbolCode : gameContext.model().levelCounter().symbolCodes()) {
                drawSprite(bonusSymbols[symbolCode], x, y, true);
                x -= tilesPx(2); // symbols are drawn from right to left
            }
        }

        if (hud.isLivesCounterShown()) {
            final RectShort sprite = spriteSheet().sprite(SpriteID.LIVES_COUNTER_SYMBOL);
            final float x = tilesPx(2);
            final float y = scene.unscaledHeight() - tilesPx(2);
            for (int i = 0; i < hud.visibleLifeCount(); ++i) {
                drawSprite(sprite, x + i * tilesPx(2), y, true);
            }
            final int lifeCount = gameContext.model().lives().count();
            if (lifeCount > hud.maxLivesShown()) {
                // show text indicating that more lives are available than symbols displayed (cheating may cause this)
                Font hintFont = Font.font("Serif", FontWeight.BOLD, scaled(8));
                fillText("%d".formatted(lifeCount), ARCADE_YELLOW, hintFont, x - 14, y + TS);
            }
        }

        if (hud.isCreditShown()) {
            fillText("CREDIT %2d".formatted(hud.credit()), ARCADE_WHITE, arcadeFont8(), tilesPx(2), scene.unscaledHeight());
        }
    }

    private void drawScore(Score score, String title, Font font, Color color, double x, double y) {
        fillText(title, color, font, x, y);
        fillText("%7s".formatted("%02d".formatted(score.points())), color, font, x, y + TS + 1);
        if (score.points() != 0) {
            fillText("L" + score.levelNumber(), color, font, x + tilesPx(8), y + TS + 1);
        }
    }
}