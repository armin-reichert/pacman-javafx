/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.entities.LevelCounter;
import de.amr.pacmanfx.core.entities.LivesCounter;
import de.amr.pacmanfx.core.entities.Score;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
import de.amr.pacmanfx.ui.gamescene.d2.HUD_Renderer;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_WHITE;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_YELLOW;
import static java.util.Objects.requireNonNull;

public class Arcade_HUD_Renderer
    extends BaseRenderer
    implements SpriteRenderer, HUD_Renderer {

    public static final String SCORE_TEXT = "SCORE";
    public static final String HIGH_SCORE_TEXT = "HIGH SCORE";
    public static final String CREDIT_TEXT_PATTERN = "CREDIT %2d";

    private static final Color SCORE_TEXT_COLOR = ARCADE_WHITE;
    private static final Color SCORE_TEXT_COLOR_DISABLED = Color.GRAY;

    protected final SpriteSheet spriteSheet;
    protected final RectShort livesCounterSymbolSprite;
    protected final RectShort[] bonusSymbolSprites;

    public Arcade_HUD_Renderer(
        Canvas canvas,
        SpriteSheet spriteSheet,
        RectShort livesCounterSymbolSprite,
        RectShort[] bonusSymbolSprites) {

        super(canvas);
        this.spriteSheet = requireNonNull(spriteSheet);
        this.livesCounterSymbolSprite = requireNonNull(livesCounterSymbolSprite);
        this.bonusSymbolSprites = requireNonNull( bonusSymbolSprites);
    }

    @Override
    public SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public void drawHUD(HUD hud, GameSession session, GameScene gameScene, long tick) {
        requireNonNull(hud);
        requireNonNull(session);
        requireNonNull(gameScene);

        if (!hud.isVisible()) {
            return;
        }

        if (gameScene.optCanvasRendering().isEmpty()) {
            return;
        }
        final CanvasRenderingComp canvasRendering = gameScene.reqCanvasRendering();

        if (hud.gameScore().isVisible()) {
            drawScores(hud.gameScore(), hud.highScore(), session);
        }

        if (hud.levelCounter().isVisible()) {
            drawLevelCounter(hud.levelCounter(), canvasRendering);
        }

        if (hud.livesCounter().isVisible()) {
            drawLivesCounter(hud.livesCounter(), session, canvasRendering);
        }

        if (hud.isCreditVisible()) {
            drawCredit(gameScene.game().coinMechanism(), canvasRendering);
        }
    }

    private void drawScores(Score gameScore, Score highScore, GameSession session) {
        drawScore(gameScore, SCORE_TEXT, arcadeFont8(), SCORE_TEXT_COLOR, tilesPx(1), tilesPx(1));

        Color color = SCORE_TEXT_COLOR;
        if (!session.isAttractMode() && !highScore.data().isEnabled()) {
            color = SCORE_TEXT_COLOR_DISABLED;
        }
        drawScore(highScore, HIGH_SCORE_TEXT, arcadeFont8(), color, tilesPx(14), tilesPx(1));
    }

    private void drawScore(Score score, String title, Font font, Color color, double x, double y) {
        fillText(title, color, font, x, y);
        fillText("%7s".formatted("%02d".formatted(score.data().points())), color, font, x, y + TS + 1);
        if (score.data().points() != 0) {
            fillText("L" + score.data().levelNumber(), color, font, x + tilesPx(8), y + TS + 1);
        }
    }

    private void drawLivesCounter(LivesCounter livesCounter, GameSession session, CanvasRenderingComp canvasRendering) {
        final int numLives = session.numLives();
        final int displayedSymbolsCount = Math.min(numLives - 1, livesCounter.data().maxLivesShown());

        final float x = tilesPx(2);
        final float y = canvasRendering.unscaledHeight() - tilesPx(2);
        final float spacing = tilesPx(2);
        // Draw at most (numLives - 1) symbols in lives counter
        for (int i = 0; i < displayedSymbolsCount; ++i) {
            drawSprite(livesCounterSymbolSprite, x + i * spacing, y, true);
        }
        if (numLives - 1 > livesCounter.data().maxLivesShown()) {
            // Show text indicating that more lives are available than symbols displayed (cheating may cause this)
            final Font font = Font.font("Serif", FontWeight.BOLD, scaled(8));
            fillText("%d".formatted(numLives), ARCADE_YELLOW, font, x - 14, y + TS);
        }
    }
    private void drawLevelCounter(LevelCounter levelCounter, CanvasRenderingComp canvasRendering) {
        final float y = canvasRendering.unscaledHeight() - tilesPx(2) + 2;
        float x = canvasRendering.unscaledWidth() - tilesPx(4);
        for (int symbolCode : levelCounter.data().symbolCodes()) {
            drawSprite(bonusSymbolSprites[symbolCode], x, y, true);
            x -= tilesPx(2); // symbols are drawn from right to left
        }
    }

    private void drawCredit(CoinMechanism coinMechanism, CanvasRenderingComp canvasRendering) {
        final int credit = coinMechanism.numCoins();
        final String text = CREDIT_TEXT_PATTERN.formatted(credit);
        fillText(text, ARCADE_WHITE, arcadeFont8(), tilesPx(2), canvasRendering.unscaledHeight());
    }
}
