/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.CreditDisplay;
import de.amr.pacmanfx.core.entities.LevelCounter;
import de.amr.pacmanfx.core.entities.LivesCounter;
import de.amr.pacmanfx.core.entities.Score;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.HUD_Renderer;
import de.amr.pacmanfx.ui.gamescene.d2.HUD_Style;
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

public class Arcade_HUD_Renderer extends BaseRenderer implements SpriteRenderer, HUD_Renderer {

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
    public void render(Object r, long tick) {
        //TODO
    }

    @Override
    public void drawHUD(HUD hud, GameSession session, GameScene gameScene, long tick) {
        //TODO remove this method
    }

    @Override
    public void drawHUDEntity(GameEntity entity, GameContext game) {
        requireNonNull(entity);
        requireNonNull(game);

        if (!entity.isVisible()) return;
        final GameSession session = game.session();
        switch (entity) {
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
            case CreditDisplay creditDisplay -> {
                //TODO update component elsewhere
                creditDisplay.data().setCredit(game.coinMechanism().numCoins());
                drawCreditDisplay(creditDisplay);
            }
            default -> throw new IllegalStateException("Unexpected value: " + entity);
        }
    }

    private void drawCreditDisplay(CreditDisplay creditDisplay) {
        if (creditDisplay.isVisible()) {
            final int credit = creditDisplay.data().credit();
            final Font scaledFont = Ufx.scaleFontBy(style.scoreTextFont(), scaling());
            final String text = style.creditTextFormat().formatted(credit);
            final float baseline = creditDisplay.pos().y();
            fillText(text, ARCADE_WHITE, scaledFont, creditDisplay.pos().x(), baseline);
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
        final float y = levelCounter.pos().y();
        float x = levelCounter.pos().x();
        for (int symbolCode : levelCounter.data().symbolCodes()) {
            drawSprite(style.bonusSymbolSprites()[symbolCode], x, y, true);
            x -= tilesPx(2); // symbols are drawn from right to left
        }
    }
}
