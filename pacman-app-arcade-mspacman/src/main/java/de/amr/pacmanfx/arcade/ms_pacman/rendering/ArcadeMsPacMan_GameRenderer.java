/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.arcade.ms_pacman.scenes.Clapperboard;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.Marquee;
import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.LevelCounter;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.MovingBonus;
import de.amr.pacmanfx.ui._2d.SpriteGameRenderer;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.function.Predicate;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.arcade.rendering.ArcadePalette.ARCADE_WHITE;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static de.amr.pacmanfx.ui.PacManGames.theAssets;
import static de.amr.pacmanfx.ui.PacManGames.theUI;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class ArcadeMsPacMan_GameRenderer implements SpriteGameRenderer {

    private final BrightMazesSpriteSheet brightMazesSpriteSheet;
    private final ArcadeMsPacMan_SpriteSheet spriteSheet;
    private final GraphicsContext ctx;
    private final FloatProperty scalingPy = new SimpleFloatProperty(1.0f);
    private int colorMapIndex;

    public ArcadeMsPacMan_GameRenderer(
        ArcadeMsPacMan_SpriteSheet spriteSheet,
        BrightMazesSpriteSheet brightMazesSpriteSheet,
        Canvas canvas)
    {
        this.spriteSheet = requireNonNull(spriteSheet);
        this.brightMazesSpriteSheet = requireNonNull(brightMazesSpriteSheet);
        ctx = requireNonNull(canvas).getGraphicsContext2D();
        colorMapIndex = -1; // undefined
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() { return spriteSheet; }

    @Override
    public GraphicsContext ctx() { return ctx; }

    @Override
    public FloatProperty scalingProperty() { return scalingPy; }

    @Override
    public void applyRenderingHints(GameLevel level) {
        colorMapIndex = level.worldMap().getConfigValue("colorMapIndex");
    }

    @Override
    public void drawLevel(GameLevel level, double x, double y, Color backgroundColor, boolean mazeHighlighted, boolean energizerHighlighted) {
        if (mazeHighlighted) {
            drawSpriteScaled(
                brightMazesSpriteSheet.sourceImage(),
                brightMazesSpriteSheet.spriteSeq(BrightMazesSpriteSheet.BRIGHT_MAZES_ID)[colorMapIndex], x, y);
        } else if (level.uneatenFoodCount() == 0) {
            drawSpriteScaled(spriteSheet.spriteSeq(SpriteID.EMPTY_MAZES)[colorMapIndex], x, y);
        } else {
            drawSpriteScaled(spriteSheet.spriteSeq(SpriteID.FULL_MAZES)[colorMapIndex], x, y);
            ctx.save();
            ctx.scale(scaling(), scaling());
            overPaintEatenPelletTiles(level, backgroundColor);
            overPaintEnergizerTiles(level, tile -> !energizerHighlighted || level.tileContainsEatenFood(tile), backgroundColor);
            ctx.restore();
        }
    }

    /**
     * Over-paints all eaten pellet tiles.
     * Assumes to be called in scaled graphics context!
     *
     * @param level the game level
     * @param color over-paint color (background color of level)
     */
    private void overPaintEatenPelletTiles(GameLevel level, Color color) {
        level.worldMap().tiles()
            .filter(not(level::isEnergizerPosition))
            .filter(level::tileContainsEatenFood)
            .forEach(tile -> paintSquareInsideTile(tile, 4, color));
    }

    /**
     * Over-paints all eaten energizer tiles.
     * Assumes to be called in scaled graphics context!
     *
     * @param level the game level
     * @param overPaintCondition when {@code true} energizer tile is over-painted
     * @param color over-paint color (background color of level)
     */
    private void overPaintEnergizerTiles(GameLevel level, Predicate<Vector2i> overPaintCondition, Color color) {
        level.energizerTiles().filter(overPaintCondition)
            .forEach(tile -> paintSquareInsideTile(tile, 10, color));
    }

    /**
     * Draws a square of the given size in background color over the tile. Used to hide eaten food and energizers.
     * Assumes to be called in scaled graphics context!
     */
    private void paintSquareInsideTile(Vector2i tile, double squareSize, Color color) {
        double centerX = tile.x() * TS + HTS, centerY = tile.y() * TS + HTS;
        ctx().setFill(color);
        ctx().fillRect(centerX - 0.5 * squareSize, centerY - 0.5 * squareSize, squareSize, squareSize);
    }

    private void drawLevelCounter(LevelCounter levelCounter) {
        float x = levelCounter.x(), y = levelCounter.y();
        for (byte symbol : levelCounter.symbols()) {
            Sprite sprite = theUI().configuration().createBonusSymbolSprite(symbol);
            drawSpriteScaled(sprite, x, y);
            x -= TS * 2;
        }
    }

    @Override
    public void drawActor(Actor actor) {
        switch (actor) {
            case Clapperboard clapperboard -> drawClapperBoard(clapperboard);
            case LevelCounter levelCounter -> drawLevelCounter(levelCounter);
            case Marquee marquee           -> drawMarquee(marquee);
            case MovingBonus movingBonus   -> drawMovingBonus(movingBonus);
            default -> SpriteGameRenderer.super.drawActor(actor);
        }
    }

    private void drawMovingBonus(MovingBonus bonus) {
        if (!bonus.isVisible()) {
            return;
        }
        ctx.save();
        ctx.setImageSmoothing(false);
        ctx.translate(0, bonus.elongationY());
        switch (bonus.state()) {
            case Bonus.STATE_EDIBLE -> {
                Sprite sprite = theUI().configuration().createBonusSymbolSprite(bonus.symbol());
                drawActorSprite(bonus.actor(), sprite);
            }
            case Bonus.STATE_EATEN  -> {
                Sprite sprite = theUI().configuration().createBonusValueSprite(bonus.symbol());
                drawActorSprite(bonus.actor(), sprite);
            }
        }
        ctx.restore();
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

    public void drawMsPacManCopyrightAtTile(Color color, Font font, int tileX, int tileY) {
        Image image = theAssets().get("ms_pacman.logo.midway");
        double x = tiles_to_px(tileX), y = tiles_to_px(tileY);
        ctx.drawImage(image, scaled(x), scaled(y + 2), scaled(tiles_to_px(4) - 2), scaled(tiles_to_px(4)));
        ctx.setFont(font);
        ctx.setFill(color);
        ctx.fillText("©", scaled(x + TS * 5), scaled(y + TS * 2 + 2));
        ctx.fillText("MIDWAY MFG CO", scaled(x + TS * 7), scaled(y + TS * 2));
        ctx.fillText("1980/1981", scaled(x + TS * 8), scaled(y + TS * 4));
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
            drawBulb(marquee, bulbIndex);
        }
        int firstBrightIndex = (int) (tick % marquee.totalBulbCount());
        ctx.setFill(marquee.bulbOnColor());
        for (int i = 0; i < marquee.brightBulbsCount(); ++i) {
            drawBulb(marquee, (firstBrightIndex + i * marquee.brightBulbsDistance()) % marquee.totalBulbCount());
        }
        // simulate bug from original Arcade game
        ctx.setFill(marquee.bulbOffColor());
        for (int bulbIndex = 81; bulbIndex < marquee.totalBulbCount(); bulbIndex += 2) {
            drawBulb(marquee, bulbIndex);
        }
    }

    private void drawBulb(Marquee marquee, int bulbIndex) {
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
}