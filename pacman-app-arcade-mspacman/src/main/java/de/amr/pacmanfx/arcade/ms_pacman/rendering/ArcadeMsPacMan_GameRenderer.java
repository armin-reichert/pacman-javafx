/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.Clapperboard;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.Marquee;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.MidwayCopyright;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.BonusState;
import de.amr.pacmanfx.ui.GameAssets;
import de.amr.pacmanfx.ui._2d.GameRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.*;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class ArcadeMsPacMan_GameRenderer extends GameRenderer {

    protected GameAssets assets;
    protected ArcadeMsPacMan_SpriteSheet spriteSheet;
    protected BrightMazesSpriteSheet brightMazesSpriteSheet;

    public ArcadeMsPacMan_GameRenderer(
        GameAssets assets,
        Canvas canvas,
        ArcadeMsPacMan_SpriteSheet spriteSheet,
        BrightMazesSpriteSheet brightMazesSpriteSheet)
    {
        this.assets = requireNonNull(assets);
        this.spriteSheet = requireNonNull(spriteSheet);
        this.brightMazesSpriteSheet = requireNonNull(brightMazesSpriteSheet);
        setCanvas(canvas);
    }

    protected ArcadeMsPacMan_GameRenderer(GameAssets assets, Canvas canvas, ArcadeMsPacMan_SpriteSheet spriteSheet) {
        this(assets, canvas, spriteSheet, null);
    }

    @Override
    public void drawHUD(GameContext gameContext, HUDData data, Vector2f sceneSize, long tick) {
        if (!data.isVisible()) return;

        if (data.isScoreVisible()) {
            Color color = ARCADE_WHITE;
            Font font = assets.arcadeFont(scaled(8));
            drawScore(gameContext.game().scoreManager().score(), "SCORE", color, font, TS(1), TS(1));
            drawScore(gameContext.game().scoreManager().highScore(), "HIGH SCORE", color, font, TS(14), TS(1));
        }

        if (data.isLevelCounterVisible()) {
            LevelCounter levelCounter = data.theLevelCounter();
            float x = sceneSize.x() - TS(4), y = sceneSize.y() - TS(2);
            for (byte symbol : levelCounter.symbols()) {
                RectShort sprite = spriteSheet.spriteSequence(SpriteID.BONUS_SYMBOLS)[symbol];
                drawSprite(spriteSheet.sourceImage(), sprite, x, y, true);
                x -= TS(2);
            }
        }

        if (data.isLivesCounterVisible()) {
            float x = TS(2), y = sceneSize.y() - TS(2);
            LivesCounter livesCounter = data.theLivesCounter();
            RectShort sprite = spriteSheet.sprite(SpriteID.LIVES_COUNTER_SYMBOL);
            for (int i = 0; i < livesCounter.visibleLifeCount(); ++i) {
                drawSprite(spriteSheet.sourceImage(), sprite, x + TS(2 * i), y, true);
            }
            if (gameContext.game().lifeCount() > livesCounter.maxLivesDisplayed()) {
                // show text indicating that more lives are available than symbols displayed (cheating may cause this)
                Font font = Font.font("Serif", FontWeight.BOLD, scaled(8));
                fillText("%d".formatted(gameContext.game().lifeCount()), ARCADE_YELLOW, font, x - 14, y + TS);
            }
        }

        if (data.isCreditVisible()) {
            String text = "CREDIT %2d".formatted(gameContext.coinMechanism().numCoins());
            fillText(text, ARCADE_WHITE, assets.arcadeFont(scaled(8)), TS(2), sceneSize.y() - 2);
        }
    }

    private void drawScore(Score score, String title, Color color, Font font, double x, double y) {
        fillText(title, color, font, x, y);
        fillText("%7s".formatted("%02d".formatted(score.points())), color, font, x, y + TS + 1);
        if (score.points() != 0) {
            fillText("L" + score.levelNumber(), color, font, x + TS(8), y + TS + 1);
        }
    }

    @Override
    public void drawLevel(
        GameContext gameContext,
        GameLevel level,
        Color backgroundColor,
        boolean mazeHighlighted,
        boolean energizerHighlighted,
        long tick)
    {
        final int colorMapIndex = level.worldMap().getConfigValue("colorMapIndex");
        if (mazeHighlighted) {
            RectShort[] brightMazes = brightMazesSpriteSheet.spriteSequence(BrightMazesSpriteSheet.SpriteID.BRIGHT_MAZES);
            RectShort maze = brightMazes[colorMapIndex];
            drawSprite(brightMazesSpriteSheet.sourceImage(), maze, 0, TS(GameLevel.EMPTY_ROWS_OVER_MAZE), true);
        } else if (level.uneatenFoodCount() == 0) {
            RectShort maze = spriteSheet.spriteSequence(SpriteID.EMPTY_MAZES)[colorMapIndex];
            drawSprite(spriteSheet.sourceImage(), maze, 0, TS(GameLevel.EMPTY_ROWS_OVER_MAZE), true);
        } else {
            RectShort mazeSprite = spriteSheet.spriteSequence(SpriteID.FULL_MAZES)[colorMapIndex];
            drawSprite(spriteSheet.sourceImage(), mazeSprite, 0, TS(GameLevel.EMPTY_ROWS_OVER_MAZE), true);
            ctx().save();
            ctx().scale(scaling(), scaling());
            ctx().setFill(backgroundColor);
            level.worldMap().tiles()
                    .filter(not(level::isEnergizerPosition))
                    .filter(level::tileContainsEatenFood)
                    .forEach(tile -> fillSquareAtTileCenter(tile, 4));
            level.energizerPositions().stream()
                    .filter(tile -> !energizerHighlighted || level.tileContainsEatenFood(tile))
                    .forEach(tile -> fillSquareAtTileCenter(tile, 10));
            ctx().restore();
        }
    }

    @Override
    public void drawActor(Actor actor, Image spriteSheetImage) {
        requireNonNull(actor);
        if (actor.isVisible()) {
            switch (actor) {
                case Clapperboard clapperboard -> drawClapperBoard(clapperboard);
                case Marquee marquee           -> drawMarquee(marquee);
                case MidwayCopyright copyright -> drawMidwayCopyright(copyright);
                case Bonus bonus -> drawMovingBonus(bonus);
                default -> super.drawActor(actor, spriteSheetImage);
            }
        }
    }

    public void drawMovingBonus(Bonus bonus) {
        if (bonus.state() == BonusState.INACTIVE) return;
        ctx().save();
        ctx().translate(0, bonus.jumpHeight());
        switch (bonus.state()) {
            case EDIBLE-> {
                RectShort sprite = spriteSheet.spriteSequence(SpriteID.BONUS_SYMBOLS)[bonus.symbol()];
                drawSpriteCentered(bonus.center(), spriteSheet.sourceImage(), sprite);
            }
            case EATEN  -> {
                RectShort sprite = spriteSheet.spriteSequence(SpriteID.BONUS_VALUES)[bonus.symbol()];
                drawSpriteCentered(bonus.center(), spriteSheet.sourceImage(), sprite);
            }
        }
        ctx().restore();
    }

    public void drawClapperBoard(Clapperboard clapperboard) {
        if (!clapperboard.isVisible()) {
            return;
        }
        RectShort sprite = spriteSheet.spriteSequence(SpriteID.CLAPPERBOARD)[clapperboard.state()];
        double numberX = scaled(clapperboard.x() + sprite.width() - 25);
        double numberY = scaled(clapperboard.y() + 18);
        double textX = scaled(clapperboard.x() + sprite.width());
        drawSpriteScaledCenteredAt(spriteSheet.sourceImage(), sprite, clapperboard.x() + HTS, clapperboard.y() + HTS);
        ctx().setFont(clapperboard.font());
        ctx().setFill(ARCADE_WHITE);
        ctx().fillText(clapperboard.number(), numberX, numberY);
        ctx().fillText(clapperboard.text(), textX, numberY);
    }

    /**
     * 6 of the 96 light bulbs are bright in each frame, shifting counter-clockwise every tick.
     * <p>
     * The bulbs on the left border however are switched off every second frame. This is
     * probably a bug in the original Arcade game.
     * </p>
     */
    public void drawMarquee(Marquee marquee) {
        long tick = marquee.timer().tickCount();
        ctx().setFill(marquee.bulbOffColor());
        for (int bulbIndex = 0; bulbIndex < marquee.totalBulbCount(); ++bulbIndex) {
            drawMarqueeBulb(marquee, bulbIndex);
        }
        int firstBrightIndex = (int) (tick % marquee.totalBulbCount());
        ctx().setFill(marquee.bulbOnColor());
        for (int i = 0; i < marquee.brightBulbsCount(); ++i) {
            drawMarqueeBulb(marquee, (firstBrightIndex + i * marquee.brightBulbsDistance()) % marquee.totalBulbCount());
        }
        // simulate bug from original Arcade game
        ctx().setFill(marquee.bulbOffColor());
        for (int bulbIndex = 81; bulbIndex < marquee.totalBulbCount(); bulbIndex += 2) {
            drawMarqueeBulb(marquee, bulbIndex);
        }
    }

    private void drawMarqueeBulb(Marquee marquee, int bulbIndex) {
        final double minX = marquee.x(), minY = marquee.y();
        final double maxX = marquee.x() + marquee.width(), maxY = marquee.y() + marquee.height();
        double x, y;
        if (bulbIndex <= 33) { // lower edge left-to-right
            x = minX + 4 * bulbIndex;
            y = maxY;
        }
        else if (bulbIndex <= 48) { // right edge bottom-to-top
            x = maxX;
            y = 4 * (70 - bulbIndex);
        }
        else if (bulbIndex <= 81) { // upper edge right-to-left
            x = 4 * (marquee.totalBulbCount() - bulbIndex);
            y = minY;
        }
        else { // left edge top-to-bottom
            x = minX;
            y = 4 * (bulbIndex - 59);
        }
        ctx().fillRect(scaled(x), scaled(y), scaled(2), scaled(2));
    }

    public void drawMidwayCopyright(MidwayCopyright copyright) {
        double x = scaled(copyright.x()), y = scaled(copyright.y());
        ctx().drawImage(copyright.logo(), x, y + 2, scaled(TS(4) - 2), scaled(TS(4)));
        ctx().setFont(assets.arcadeFont(scaled(8)));
        ctx().setFill(ARCADE_RED);
        ctx().fillText("©", x + scaled(TS(5)), y + scaled(TS(2) + 2));
        ctx().fillText("MIDWAY MFG CO", x + scaled(TS(7)), y + scaled(TS(2)));
        ctx().fillText("1980/1981", x + scaled(TS(8)), y + scaled(TS(4)));
    }
}