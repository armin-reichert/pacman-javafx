/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.rendering.BaseSpriteRenderer;
import de.amr.pacmanfx.uilib.rendering.HUDRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.ARCADE_WHITE;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.ARCADE_YELLOW;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_HUDRenderer extends BaseSpriteRenderer implements HUDRenderer {

    protected final GameUI_Config uiConfig;

    public ArcadeMsPacMan_HUDRenderer(Canvas canvas, GameUI_Config uiConfig) {
        super(canvas, uiConfig.spriteSheet());
        this.uiConfig = requireNonNull(uiConfig);
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return (ArcadeMsPacMan_SpriteSheet) uiConfig.spriteSheet();
    }

    @Override
    public void drawHUD(GameContext gameContext, HUDControlData hudControlData, Vector2f sceneSize) {
        if (!hudControlData.isVisible()) return;

        Game game = gameContext.game();

        if (hudControlData.isScoreVisible()) {
            ScoreManager scoreManager = game.scoreManager();
            drawScore(scoreManager.score(), "SCORE", arcadeFontTS(), TS(1), TS(1));
            drawScore(scoreManager.highScore(), "HIGH SCORE", arcadeFontTS(), TS(14), TS(1));
        }

        if (hudControlData.isLevelCounterVisible()) {
            RectShort[] bonusSymbols = spriteSheet().spriteSequence(SpriteID.BONUS_SYMBOLS);
            float x = sceneSize.x() - TS(4), y = sceneSize.y() - TS(2) + 2;
            for (byte symbol : game.levelCounterSymbols()) {
                drawSprite(bonusSymbols[symbol], x, y, true);
                x -= TS(2); // symbols are drawn from right to left
            }
        }

        if (hudControlData.isLivesCounterVisible()) {
            RectShort sprite = spriteSheet().sprite(SpriteID.LIVES_COUNTER_SYMBOL);
            float x = TS(2), y = sceneSize.y() - TS(2);
            for (int i = 0; i < game.visibleLifeCount(); ++i) {
                drawSprite(sprite, x + i * TS(2), y, true);
            }
            int lifeCount = game.lifeCount();
            if (lifeCount > game.maxLivesDisplayed()) {
                // show text indicating that more lives are available than symbols displayed (cheating may cause this)
                Font hintFont = Font.font("Serif", FontWeight.BOLD, scaled(8));
                fillText("%d".formatted(lifeCount), ARCADE_YELLOW, hintFont, x - 14, y + TS);
            }
        }

        if (hudControlData.isCreditVisible()) {
            int credit = gameContext.coinMechanism().numCoins();
            fillText("CREDIT %2d".formatted(credit), ARCADE_WHITE, arcadeFontTS(), TS(2), sceneSize.y());
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
