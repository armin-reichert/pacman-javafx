/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.HeadsUpDisplay;
import de.amr.pacmanfx.model.Score;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.api.ArcadePalette.ARCADE_WHITE;
import static de.amr.pacmanfx.ui.api.ArcadePalette.ARCADE_YELLOW;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_HeadsUpDisplay_Renderer extends BaseRenderer implements SpriteRenderer, HeadsUpDisplay_Renderer {

    public static final String SCORE_TEXT = "SCORE";
    public static final String HIGH_SCORE_TEXT = "HIGH SCORE";
    public static final String CREDIT_TEXT_PATTERN = "CREDIT %2d";

    private static final Color SCORE_TEXT_COLOR = ARCADE_WHITE;
    private static final Color SCORE_TEXT_COLOR_DISABLED = Color.RED;

    public ArcadePacMan_HeadsUpDisplay_Renderer(Canvas canvas) {
        super(canvas);
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.INSTANCE;
    }

    @Override
    public void draw(HeadsUpDisplay hud, Game game, GameScene2D scene) {
        requireNonNull(game);
        requireNonNull(scene);

        final Vector2i sceneSize = scene.unscaledSize();

        if (!hud.isVisible()) return;

        if (hud.isScoreVisible()) {
            drawScore(game.score(), SCORE_TEXT, arcadeFont8(), SCORE_TEXT_COLOR, TS(1), TS(1));

            final Score highScore = game.highScore();
            Color color = SCORE_TEXT_COLOR;
            if (game.optGameLevel().isPresent() && !game.level().isDemoLevel() && !highScore.isEnabled()) {
                color = SCORE_TEXT_COLOR_DISABLED;
            }
            drawScore(highScore, HIGH_SCORE_TEXT, arcadeFont8(), color, TS(14), TS(1));
        }

        if (hud.isLevelCounterVisible()) {
            final RectShort[] bonusSymbolSprites = spriteSheet().spriteSequence(SpriteID.BONUS_SYMBOLS);
            final float y = sceneSize.y() - TS(2) + 2;
            float x = sceneSize.x() - TS(4);
            for (byte symbol : game.levelCounterSymbols()) {
                drawSprite(bonusSymbolSprites[symbol], x, y, true);
                x -= TS(2); // symbols are drawn from right to left
            }
        }

        if (hud.isLivesCounterVisible()) {
            final RectShort livesCounterSprite = spriteSheet().sprite(SpriteID.LIVES_COUNTER_SYMBOL);
            final float x = TS(2);
            final float y = sceneSize.y() - TS(2);
            for (int i = 0; i < hud.visibleLifeCount(); ++i) {
                drawSprite(livesCounterSprite, x + i * TS(2), y, true);
            }
            final int lifeCount = game.lifeCount();
            if (lifeCount > hud.maxLivesDisplayed()) {
                // Show text indicating that more lives are available than symbols displayed (cheating may cause this)
                final Font font = Font.font("Serif", FontWeight.BOLD, scaled(8));
                fillText("%d".formatted(lifeCount), ARCADE_YELLOW, font, x - 14, y + TS);
            }
        }

        if (hud.isCreditVisible()) {
            fillText(CREDIT_TEXT_PATTERN.formatted(hud.numCoins()), ARCADE_WHITE, arcadeFont8(), TS(2), sceneSize.y());
        }
    }

    private void drawScore(Score score, String title, Font font, Color color, double x, double y) {
        fillText(title, color, font, x, y);
        fillText("%7s".formatted("%02d".formatted(score.points())), color, font, x, y + TS + 1);
        if (score.points() != 0) {
            fillText("L" + score.levelNumber(), color, font, x + TS(8), y + TS + 1);
        }
    }
}