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
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.ui._2d.DebugInfoRenderer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.rendering.GameLevelRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.ARCADE_RED;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.ARCADE_WHITE;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class ArcadeMsPacMan_GameLevelRenderer extends GameLevelRenderer<SpriteID> implements DebugInfoRenderer {

    protected GameUI_Config uiConfig;
    protected BrightMazesSpriteSheet brightMazesSpriteSheet;

    private final RectShort[] bonusSymbols;
    private final RectShort[] bonusValues;

    public ArcadeMsPacMan_GameLevelRenderer(Canvas canvas, GameUI_Config uiConfig, BrightMazesSpriteSheet brightMazesSpriteSheet) {
        super(canvas);
        this.uiConfig = requireNonNull(uiConfig);
        this.brightMazesSpriteSheet = brightMazesSpriteSheet; // can be null in Ms. Pac-Man XXL!

        bonusSymbols = spriteSheet().spriteSequence(SpriteID.BONUS_SYMBOLS);
        bonusValues = spriteSheet().spriteSequence(SpriteID.BONUS_VALUES);
    }

    protected ArcadeMsPacMan_GameLevelRenderer(Canvas canvas, GameUI_Config uiConfig) {
        this(canvas, uiConfig, null);
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return (ArcadeMsPacMan_SpriteSheet) uiConfig.spriteSheet();
    }

    @Override
    public void drawGameLevel(GameContext gameContext, Color backgroundColor, boolean mazeBright, boolean energizerBright) {
        ctx().setFill(backgroundColor);
        if (mazeBright) {
            drawBrightGameLevel(gameContext.gameLevel());
        } else if (gameContext.gameLevel().uneatenFoodCount() == 0) {
            drawEmptyGameLevel(gameContext.gameLevel());
        } else {
            drawGameLevelWithFood(gameContext.gameLevel(), !energizerBright);
        }
    }

    private void drawEmptyGameLevel(GameLevel gameLevel) {
        int colorMapIndex = gameLevel.worldMap().getConfigValue("colorMapIndex");
        RectShort maze = spriteSheet().spriteSequence(SpriteID.EMPTY_MAZES)[colorMapIndex];
        drawSprite(maze, 0, TS(GameLevel.EMPTY_ROWS_OVER_MAZE), true);
    }

    private void drawBrightGameLevel(GameLevel gameLevel) {
        if (brightMazesSpriteSheet != null) {
            int colorMapIndex = gameLevel.worldMap().getConfigValue("colorMapIndex");
            RectShort[] brightMazes = brightMazesSpriteSheet.spriteSequence(BrightMazesSpriteSheet.SpriteID.BRIGHT_MAZES);
            RectShort maze = brightMazes[colorMapIndex];
            //drawSprite(brightMazesSpriteSheet, maze, 0, TS(GameLevel.EMPTY_ROWS_OVER_MAZE), true);
            double s = scaling();
            ctx().drawImage(spriteSheet().sourceImage(),
                maze.x(), maze.y(), maze.width(), maze.height(),
                0, s * TS(GameLevel.EMPTY_ROWS_OVER_MAZE), s * maze.width(), s * maze.height());
        }
    }

    private void drawGameLevelWithFood(GameLevel gameLevel, boolean energizerDark) {
        int colorMapIndex = gameLevel.worldMap().getConfigValue("colorMapIndex");
        // Draw the maze
        RectShort mazeSprite = spriteSheet().spriteSequence(SpriteID.FULL_MAZES)[colorMapIndex];
        drawSprite(mazeSprite, 0, TS(GameLevel.EMPTY_ROWS_OVER_MAZE), true);
        ctx().save();
        ctx().scale(scaling(), scaling());
        // Overpaint the eaten pellets as they are part of the maze image
        gameLevel.worldMap().tiles()
                .filter(not(gameLevel::isEnergizerPosition))
                .filter(gameLevel::tileContainsEatenFood)
                .forEach(tile -> fillSquareAtTileCenter(tile, 4));
        // Draw the energizers, overpaint them if they are in dark phase
        gameLevel.energizerPositions().stream()
                .filter(tile -> energizerDark || gameLevel.tileContainsEatenFood(tile))
                .forEach(tile -> fillSquareAtTileCenter(tile, 10));
        ctx().restore();
    }

    @Override
    public void drawActor(Actor actor) {
        requireNonNull(actor);
        if (actor.isVisible()) {
            switch (actor) {
                case Clapperboard clapperboard -> drawClapperBoard(clapperboard);
                case Marquee marquee           -> drawMarquee(marquee);
                case MidwayCopyright copyright -> drawMidwayCopyright(copyright);
                case Bonus bonus -> drawMovingBonus(bonus);
                default -> super.drawActor(actor);
            }
        }
    }

    public void drawMovingBonus(Bonus bonus) {
        switch (bonus.state()) {
            case EDIBLE -> {
                ctx().save();
                ctx().translate(0, bonus.jumpHeight());
                drawSpriteCentered(bonus.center(), bonusSymbols[bonus.symbol()]);
                ctx().restore();
            }
            case EATEN  -> drawSpriteCentered(bonus.center(), bonusValues[bonus.symbol()]);
            case INACTIVE -> {}
        }
    }

    public void drawClapperBoard(Clapperboard clapperboard) {
        if (!clapperboard.isVisible()) {
            return;
        }
        RectShort sprite = spriteSheet().spriteSequence(SpriteID.CLAPPERBOARD)[clapperboard.state()];
        double numberX = scaled(clapperboard.x() + sprite.width() - 25);
        double numberY = scaled(clapperboard.y() + 18);
        double textX = scaled(clapperboard.x() + sprite.width());
        drawSpriteCentered(clapperboard.center(), sprite);
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
        ctx().setFont(uiConfig.globalAssets().arcadeFont(scaled(TS)));
        ctx().setFill(ARCADE_RED);
        ctx().fillText("©", x + scaled(TS(5)), y + scaled(TS(2) + 2));
        ctx().fillText("MIDWAY MFG CO", x + scaled(TS(7)), y + scaled(TS(2)));
        ctx().fillText("1980/1981", x + scaled(TS(8)), y + scaled(TS(4)));
    }
}