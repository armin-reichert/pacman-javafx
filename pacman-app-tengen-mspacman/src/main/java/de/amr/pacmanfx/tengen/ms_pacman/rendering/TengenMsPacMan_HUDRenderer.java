/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.HUDData;
import de.amr.pacmanfx.model.LevelCounter;
import de.amr.pacmanfx.model.LivesCounter;
import de.amr.pacmanfx.model.ScoreManager;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_HUDData;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_LevelCounter;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.HUDRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesColor;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_HUDRenderer extends BaseRenderer implements HUDRenderer {

    protected final TengenMsPacMan_UIConfig uiConfig;

    public TengenMsPacMan_HUDRenderer(TengenMsPacMan_UIConfig uiConfig, Canvas canvas) {
        super(canvas, uiConfig.spriteSheet());
        this.uiConfig = requireNonNull(uiConfig);
    }

    @Override
    public void drawHUD(GameContext gameContext, HUDData data, Vector2f sceneSize) {
        requireNonNull(gameContext);
        requireNonNull(data);
        requireNonNull(sceneSize);

        if (!data.isVisible()) return;

        Font font = uiConfig.theUI().assets().arcadeFont(TS);
        TengenMsPacMan_GameModel game = gameContext.game();
        if (data.isScoreVisible()) {
            drawScores(gameContext, nesColor(0x20), font);
        }
        if (data.isLivesCounterVisible()) {
            drawLivesCounter(data.theLivesCounter(), game.lifeCount(), TS(2), sceneSize.y() - TS);
        }
        if (data.isLevelCounterVisible()) {
            var hudData = (TengenMsPacMan_HUDData) data;
            TengenMsPacMan_LevelCounter levelCounter = hudData.theLevelCounter();
            float x = sceneSize.x() - TS(2), y = sceneSize.y() - TS;
            drawLevelCounter(levelCounter.displayedLevelNumber(), levelCounter, x, y);
        }
    }

    public void drawScores(GameContext gameContext, Color color, Font font) {
        ScoreManager scoreManager = gameContext.game().scoreManager();
        ctx().save();
        ctx().scale(scaling(), scaling());
        ctx().setFill(color);
        ctx().setFont(font);
        // show 1/2 second, hide 1/2 second
        if (uiConfig.theUI().clock().tickCount() % 60 < 30) {
            ctx().fillText("1UP", TS(4), TS(1));
        }
        ctx().fillText("HIGH SCORE", TS(11), TS(1));
        ctx().fillText("%6d".formatted(scoreManager.score().points()), TS(2), TS(2));
        ctx().fillText("%6d".formatted(scoreManager.highScore().points()), TS(13), TS(2));
        ctx().restore();
    }

    public void drawLivesCounter(LivesCounter livesCounter, int lifeCount, float x, float y) {
        RectShort sprite = uiConfig.spriteSheet().sprite(SpriteID.LIVES_COUNTER_SYMBOL);
        for (int i = 0; i < livesCounter.visibleLifeCount(); ++i) {
            drawSprite(uiConfig.spriteSheet(), sprite, x + TS(i * 2), y, true);
        }
        if (lifeCount > livesCounter.maxLivesDisplayed()) {
            Font font = Font.font("Serif", FontWeight.BOLD, scaled(8));
            fillText("(%d)".formatted(lifeCount), nesColor(0x28), font, x + TS(10), y + TS);
        }
    }

    public void drawLevelCounter(int levelNumber, LevelCounter levelCounter, float x, float y) {
        if (levelNumber != 0) {
            drawLevelNumberBox(levelNumber, 0, y); // left box
            drawLevelNumberBox(levelNumber, x, y); // right box
        }
        RectShort[] symbolSprites = uiConfig.spriteSheet().spriteSequence(SpriteID.BONUS_SYMBOLS);
        x -= TS(2);
        // symbols are drawn from right to left!
        for (byte symbol : levelCounter.symbols()) {
            drawSprite(uiConfig.spriteSheet(), symbolSprites[symbol], x, y, true);
            x -= TS(2);
        }
    }

    // this is also used by the 3D scene
    public void drawLevelNumberBox(int number, double x, double y) {
        TengenMsPacMan_SpriteSheet spriteSheet = uiConfig.spriteSheet();
        drawSprite(spriteSheet, spriteSheet.sprite(SpriteID.LEVEL_NUMBER_BOX), x, y, true);
        int tens = number / 10, ones = number % 10;
        if (tens > 0) {
            drawSprite(uiConfig.spriteSheet(), spriteSheet.digitSprite(tens), x + 2, y + 2, true);
        }
        drawSprite(uiConfig.spriteSheet(), spriteSheet.digitSprite(ones), x + 10, y + 2, true);
    }
}
