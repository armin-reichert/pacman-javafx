/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.HUD;
import de.amr.pacmanfx.model.Score;
import de.amr.pacmanfx.model.ScoreManager;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.BaseSpriteRenderer;
import de.amr.pacmanfx.uilib.rendering.HUD_Renderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.api.ArcadePalette.ARCADE_WHITE;
import static de.amr.pacmanfx.ui.api.ArcadePalette.ARCADE_YELLOW;

public class ArcadeMsPacMan_HUDRenderer extends BaseSpriteRenderer implements HUD_Renderer {

    public ArcadeMsPacMan_HUDRenderer(Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(canvas, spriteSheet);
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return (ArcadeMsPacMan_SpriteSheet) super.spriteSheet();
    }

    @Override
    public void drawHUD(Game game, HUD hud, Vector2i sceneSize) {
        if (!hud.isVisible()) return;

        if (hud.isScoreVisible()) {
            ScoreManager scoreManager = game.scoreManager();
            drawScore(scoreManager.score(), "SCORE", arcadeFont8(), TS(1), TS(1));
            drawScore(scoreManager.highScore(), "HIGH SCORE", arcadeFont8(), TS(14), TS(1));
        }

        if (hud.isLevelCounterVisible()) {
            RectShort[] bonusSymbols = spriteSheet().spriteSequence(SpriteID.BONUS_SYMBOLS);
            float x = sceneSize.x() - TS(4), y = sceneSize.y() - TS(2) + 2;
            for (byte symbol : game.levelCounter().symbols()) {
                drawSprite(bonusSymbols[symbol], x, y, true);
                x -= TS(2); // symbols are drawn from right to left
            }
        }

        if (hud.isLivesCounterVisible()) {
            RectShort sprite = spriteSheet().sprite(SpriteID.LIVES_COUNTER_SYMBOL);
            float x = TS(2), y = sceneSize.y() - TS(2);
            for (int i = 0; i < hud.visibleLifeCount(); ++i) {
                drawSprite(sprite, x + i * TS(2), y, true);
            }
            int lifeCount = game.lifeCount();
            if (lifeCount > game.hud().maxLivesDisplayed()) {
                // show text indicating that more lives are available than symbols displayed (cheating may cause this)
                Font hintFont = Font.font("Serif", FontWeight.BOLD, scaled(8));
                fillText("%d".formatted(lifeCount), ARCADE_YELLOW, hintFont, x - 14, y + TS);
            }
        }

        if (hud.isCreditVisible()) {
            fillText("CREDIT %2d".formatted(hud.numCoins()), ARCADE_WHITE, arcadeFont8(), TS(2), sceneSize.y());
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
