/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.HUD;
import de.amr.pacmanfx.model.Score;
import de.amr.pacmanfx.model.ScoreManager;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import de.amr.pacmanfx.uilib.rendering.HUDRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.ARCADE_WHITE;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.ARCADE_YELLOW;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_HUDRenderer extends BaseRenderer implements HUDRenderer, Renderer {

    protected final GameUI_Config uiConfig;

    public ArcadePacMan_HUDRenderer(Canvas canvas, GameUI_Config uiConfig) {
        super(canvas);
        this.uiConfig = requireNonNull(uiConfig);
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return (ArcadePacMan_SpriteSheet) uiConfig.spriteSheet();
    }

    protected RectShort[] bonusSymbols() {
        return spriteSheet().spriteSequence(SpriteID.BONUS_SYMBOLS);
    }

    protected RectShort livesCounterSymbol() {
        return spriteSheet().sprite(SpriteID.LIVES_COUNTER_SYMBOL);
    }

    @Override
    public void drawHUD(Game game, HUD hud, Vector2i sceneSize) {
        if (!hud.isVisible()) return;

        if (hud.isScoreVisible()) {
            ScoreManager scoreManager = game.scoreManager();
            drawScore(scoreManager.score(), "SCORE", arcadeFontTS(), TS(1), TS(1));
            drawScore(scoreManager.highScore(), "HIGH SCORE", arcadeFontTS(), TS(14), TS(1));
        }

        if (hud.isLevelCounterVisible()) {
            RectShort[] bonusSymbols = bonusSymbols();
            float x = sceneSize.x() - TS(4), y = sceneSize.y() - TS(2) + 2;
            for (byte symbol : game.levelCounter().symbols()) {
                drawSprite(bonusSymbols[symbol], x, y, true);
                x -= TS(2); // symbols are drawn from right to left
            }
        }

        if (hud.isLivesCounterVisible()) {
            RectShort sprite = livesCounterSymbol();
            float x = TS(2), y = sceneSize.y() - TS(2);
            for (int i = 0; i < hud.visibleLifeCount(); ++i) {
                drawSprite(sprite, x + i * TS(2), y, true);
            }
            int lifeCount = game.lifeCount();
            if (lifeCount > hud.maxLivesDisplayed()) {
                // show text indicating that more lives are available than symbols displayed (cheating may cause this)
                Font hintFont = Font.font("Serif", FontWeight.BOLD, scaled(8));
                fillText("%d".formatted(lifeCount), ARCADE_YELLOW, hintFont, x - 14, y + TS);
            }
        }

        if (hud.isCreditVisible()) {
            fillText("CREDIT %2d".formatted(hud.numCoins()), ARCADE_WHITE, arcadeFontTS(), TS(2), sceneSize.y());
        }
    }

    private void drawScore(Score score, String title, Font font, double x, double y) {
        Color color = ARCADE_WHITE;
        fillText(title, color, font, x, y);
        fillText("%7s".formatted("%02d".formatted(score.points())), color, font, x, y + TS + 1);
        if (score.points() != 0) {
            fillText("L" + score.levelNumber(), color, font, x + TS(8), y + TS + 1);
        }
    }
}