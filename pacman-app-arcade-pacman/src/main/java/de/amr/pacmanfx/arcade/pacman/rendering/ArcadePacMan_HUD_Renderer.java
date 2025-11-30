/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.HUD;
import de.amr.pacmanfx.model.Score;
import de.amr.pacmanfx.model.ScoreManager;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.HUD_Renderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.api.ArcadePalette.ARCADE_WHITE;
import static de.amr.pacmanfx.ui.api.ArcadePalette.ARCADE_YELLOW;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_HUD_Renderer extends BaseRenderer implements SpriteRenderer, HUD_Renderer {

    public static final String SCORE_TEXT = "SCORE";
    public static final String HIGH_SCORE_TEXT = "HIGH SCORE";
    public static final String CREDIT_TEXT_PATTERN = "CREDIT %2d";

    private static final Color SCORE_TEXT_COLOR = ARCADE_WHITE;
    private static final Color SCORE_TEXT_COLOR_DISABLED = Color.RED;

    private final ArcadePacMan_SpriteSheet spriteSheet;

    public ArcadePacMan_HUD_Renderer(Canvas canvas, ArcadePacMan_SpriteSheet spriteSheet) {
        super(canvas);
        this.spriteSheet = requireNonNull(spriteSheet);
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public void drawHUD(Game game, Vector2i sceneSize) {
        final HUD hud = game.hud();

        if (!hud.isVisible()) return;

        if (hud.isScoreVisible()) {
            ScoreManager scoreManager = game.scoreManager();
            drawScore(scoreManager.score(), SCORE_TEXT, arcadeFont8(), TS(1), TS(1));
            drawScore(scoreManager.highScore(), HIGH_SCORE_TEXT, arcadeFont8(), TS(14), TS(1));
        }

        if (hud.isLevelCounterVisible()) {
            final RectShort[] bonusSymbolSprites = spriteSheet.spriteSequence(SpriteID.BONUS_SYMBOLS);
            float x = sceneSize.x() - TS(4), y = sceneSize.y() - TS(2) + 2;
            for (byte symbol : game.levelCounter().symbols()) {
                drawSprite(bonusSymbolSprites[symbol], x, y, true);
                x -= TS(2); // symbols are drawn from right to left
            }
        }

        if (hud.isLivesCounterVisible()) {
            final RectShort livesCounterSprite = spriteSheet.sprite(SpriteID.LIVES_COUNTER_SYMBOL);
            final float x = TS(2), y = sceneSize.y() - TS(2);
            for (int i = 0; i < hud.visibleLifeCount(); ++i) {
                drawSprite(livesCounterSprite, x + i * TS(2), y, true);
            }
            final int lifeCount = game.lifeCount();
            if (lifeCount > hud.maxLivesDisplayed()) {
                // show text indicating that more lives are available than symbols displayed (cheating may cause this)
                Font hintFont = Font.font("Serif", FontWeight.BOLD, scaled(8));
                fillText("%d".formatted(lifeCount), ARCADE_YELLOW, hintFont, x - 14, y + TS);
            }
        }

        if (hud.isCreditVisible()) {
            fillText(CREDIT_TEXT_PATTERN.formatted(hud.numCoins()), ARCADE_WHITE, arcadeFont8(), TS(2), sceneSize.y());
        }
    }

    private void drawScore(Score score, String title, Font font, double x, double y) {
        Color color = score.isEnabled() ? SCORE_TEXT_COLOR : SCORE_TEXT_COLOR_DISABLED;
        fillText(title, color, font, x, y);
        fillText("%7s".formatted("%02d".formatted(score.points())), color, font, x, y + TS + 1);
        if (score.points() != 0) {
            fillText("L" + score.levelNumber(), color, font, x + TS(8), y + TS + 1);
        }
    }
}