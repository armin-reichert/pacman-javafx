/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.arcade.ms_pacman.scenes.Clapperboard;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.Marquee;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.MidwayCopyright;
import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.ui._2d.SpriteGameRenderer;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.arcade.pacman.rendering.ArcadePalette.ARCADE_WHITE;
import static de.amr.pacmanfx.ui.PacManGames.theAssets;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class ArcadeMsPacMan_GameRenderer extends SpriteGameRenderer {

    private final BrightMazesSpriteSheet brightMazesSpriteSheet;
    private final ArcadeMsPacMan_SpriteSheet spriteSheet;
    private final GraphicsContext ctx;
    private final FloatProperty scalingPy = new SimpleFloatProperty(1.0f);

    public ArcadeMsPacMan_GameRenderer(
        ArcadeMsPacMan_SpriteSheet spriteSheet,
        BrightMazesSpriteSheet brightMazesSpriteSheet,
        Canvas canvas)
    {
        this.spriteSheet = requireNonNull(spriteSheet);
        this.brightMazesSpriteSheet = requireNonNull(brightMazesSpriteSheet);
        ctx = requireNonNull(canvas).getGraphicsContext2D();
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() { return spriteSheet; }

    @Override
    public GraphicsContext ctx() { return ctx; }

    @Override
    public FloatProperty scalingProperty() { return scalingPy; }

    @Override
    public void drawLevel(GameLevel level, Color backgroundColor, boolean mazeHighlighted, boolean energizerHighlighted) {
        final int colorMapIndex = level.worldMap().getConfigValue("colorMapIndex");
        if (mazeHighlighted) {
            Sprite maze = brightMazesSpriteSheet.spriteSeq(BrightMazesSpriteSheet.BRIGHT_MAZES_ID)[colorMapIndex];
            drawSpriteScaled(brightMazesSpriteSheet.sourceImage(), maze, 0, GameLevel.EMPTY_ROWS_OVER_MAZE * TS);
        } else if (level.uneatenFoodCount() == 0) {
            Sprite maze = spriteSheet.spriteSeq(SpriteID.EMPTY_MAZES)[colorMapIndex];
            drawSpriteScaled(maze, 0, GameLevel.EMPTY_ROWS_OVER_MAZE * TS);
        } else {
            Sprite maze = spriteSheet.spriteSeq(SpriteID.FULL_MAZES)[colorMapIndex];
            drawSpriteScaled(maze, 0, GameLevel.EMPTY_ROWS_OVER_MAZE * TS);
            ctx.save();
            ctx.scale(scaling(), scaling());
            ctx.setFill(backgroundColor);
            level.worldMap().tiles()
                    .filter(not(level::isEnergizerPosition))
                    .filter(level::tileContainsEatenFood)
                    .forEach(tile -> fillSquareAtTileCenter(tile, 4));
            level.energizerTiles()
                    .filter(tile -> !energizerHighlighted || level.tileContainsEatenFood(tile))
                    .forEach(tile -> fillSquareAtTileCenter(tile, 10));
            ctx.restore();
        }
    }

    @Override
    public void drawActor(Actor actor) {
        requireNonNull(actor);
        if (actor.isVisible()) {
            switch (actor) {
                case MidwayCopyright copyright -> drawMidwayCopyright(copyright);
                case Clapperboard clapperboard -> drawClapperBoard(clapperboard);
                case Marquee marquee -> drawMarquee(marquee);
                default -> super.drawActor(actor);
            }
        }
    }

    private void drawClapperBoard(Clapperboard clapperboard) {
        if (!clapperboard.isVisible()) {
            return;
        }
        Sprite sprite = spriteSheet.spriteSeq(SpriteID.CLAPPERBOARD)[clapperboard.state()];
        float numberX = scaled(clapperboard.x() + sprite.width() - 25);
        float numberY = scaled(clapperboard.y() + 18);
        float textX = scaled(clapperboard.x() + sprite.width());
        drawSpriteScaledCenteredAt(sprite, clapperboard.x() + HTS, clapperboard.y() + HTS);
        ctx.setFont(clapperboard.font());
        ctx.setFill(ARCADE_WHITE);
        ctx.fillText(clapperboard.number(), numberX, numberY);
        ctx.fillText(clapperboard.text(), textX, numberY);
    }

    /**
     * 6 of the 96 light bulbs are bright in each frame, shifting counter-clockwise every tick.
     * <p>
     * The bulbs on the left border however are switched off every second frame. This is
     * probably a bug in the original Arcade game.
     * </p>
     */
    private void drawMarquee(Marquee marquee) {
        long tick = marquee.timer().tickCount();
        ctx.setFill(marquee.bulbOffColor());
        for (int bulbIndex = 0; bulbIndex < marquee.totalBulbCount(); ++bulbIndex) {
            drawMarqueeBulb(marquee, bulbIndex);
        }
        int firstBrightIndex = (int) (tick % marquee.totalBulbCount());
        ctx.setFill(marquee.bulbOnColor());
        for (int i = 0; i < marquee.brightBulbsCount(); ++i) {
            drawMarqueeBulb(marquee, (firstBrightIndex + i * marquee.brightBulbsDistance()) % marquee.totalBulbCount());
        }
        // simulate bug from original Arcade game
        ctx.setFill(marquee.bulbOffColor());
        for (int bulbIndex = 81; bulbIndex < marquee.totalBulbCount(); bulbIndex += 2) {
            drawMarqueeBulb(marquee, bulbIndex);
        }
    }

    private void drawMarqueeBulb(Marquee marquee, int bulbIndex) {
        final double minX = marquee.x(), minY = marquee.y();
        final double maxX = marquee.x() + marquee.size().getWidth(), maxY = marquee.y() + marquee.size().getHeight();
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
        ctx.fillRect(scaled(x), scaled(y), scaled(2), scaled(2));
    }

    private void drawMidwayCopyright(MidwayCopyright copyright) {
        Image image = theAssets().get("ms_pacman.logo.midway");
        float x = scaled(copyright.x()), y = scaled(copyright.y());
        ctx.drawImage(image, x, y + 2, scaled(TS * 4 - 2), scaled(TS * 4));
        ctx.setFont(copyright.font());
        ctx.setFill(copyright.color());
        ctx.fillText("©", x + scaled(TS * 5), y + scaled(TS * 2 + 2));
        ctx.fillText("MIDWAY MFG CO", x + scaled(TS * 7), y + scaled(TS * 2));
        ctx.fillText("1980/1981", x + scaled(TS * 8), y + scaled(TS * 4));
    }
}