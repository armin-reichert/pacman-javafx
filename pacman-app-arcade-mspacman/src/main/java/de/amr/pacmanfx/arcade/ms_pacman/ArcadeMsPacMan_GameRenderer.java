/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.LevelCounter;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.MovingBonus;
import de.amr.pacmanfx.ui._2d.SpriteGameRenderer;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.arcade.ArcadePalette.ARCADE_WHITE;
import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_SpriteSheet.*;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static de.amr.pacmanfx.ui.PacManGames.theAssets;
import static de.amr.pacmanfx.ui.PacManGames.theUI;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_GameRenderer implements SpriteGameRenderer {

    private final ArcadeMsPacMan_SpriteSheet spriteSheet;
    private final GraphicsContext ctx;
    private final FloatProperty scalingPy = new SimpleFloatProperty(1.0f);
    private int colorMapIndex;

    public ArcadeMsPacMan_GameRenderer(ArcadeMsPacMan_SpriteSheet spriteSheet, Canvas canvas) {
        this.spriteSheet = requireNonNull(spriteSheet);
        ctx = requireNonNull(canvas).getGraphicsContext2D();
        colorMapIndex = -1; // undefined
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public GraphicsContext ctx() { return ctx; }

    @Override
    public FloatProperty scalingProperty() {
        return scalingPy;
    }

    @Override
    public void applyRenderingHints(GameLevel level) {
        colorMapIndex = level.worldMap().getConfigValue("colorMapIndex");
    }

    @Override
    public void drawLevel(GameLevel level, double x, double y, Color backgroundColor, boolean mazeHighlighted, boolean energizerHighlighted) {
        if (mazeHighlighted) {
            drawSpriteScaled(theAssets().get("ms_pacman.flashing_mazes"), HIGHLIGHTED_MAZE_SPRITES[colorMapIndex], x, y);
        } else if (level.uneatenFoodCount() == 0) {
            drawSpriteScaled(EMPTY_MAZE_SPRITES[colorMapIndex], x, y);
        } else {
            drawSpriteScaled(FULL_MAZE_SPRITES[colorMapIndex], x, y);
            ctx.save();
            ctx.scale(scaling(), scaling());
            overPaintEatenPelletTiles(level, backgroundColor);
            overPaintEnergizerTiles(level, tile -> !energizerHighlighted || level.tileContainsEatenFood(tile), backgroundColor);
            ctx.restore();
        }
    }

    @Override
    public void drawLevelCounter(LevelCounter levelCounter, Vector2f sceneSizeInPixels) {
        float x = sceneSizeInPixels.x() - 4 * TS, y = sceneSizeInPixels.y() - 2 * TS;
        for (byte symbol : levelCounter.symbols()) {
            Sprite sprite = theUI().configuration().createBonusSymbolSprite(symbol);
            drawSpriteScaled(sprite, x, y);
            x -= TS * 2;
        }
    }

    @Override
    public void drawActor(Actor actor) {
        if (actor instanceof Clapperboard clapperboard) {
            drawClapperBoard(clapperboard);
        }
        else if (actor instanceof Bonus bonus) {
            if (actor instanceof MovingBonus movingBonus) {
                drawMovingBonus(movingBonus);
            } else {
                Logger.warn("Cannot draw bonus of type {}", bonus.getClass().getSimpleName());
            }
        }
        else {
            SpriteGameRenderer.super.drawActor(actor);
        }
    }

    private void drawMovingBonus(MovingBonus bonus) {
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
        clapperboard.currentSprite().ifPresent(sprite -> {
            float numberX = scaled(clapperboard.x() + sprite.width() - 25);
            float numberY = scaled(clapperboard.y() + 18);
            float textX = scaled(clapperboard.x() + sprite.width());
            drawSpriteScaledCenteredAt(sprite, clapperboard.x() + HTS, clapperboard.y() + HTS);
            ctx.setFont(clapperboard.font());
            ctx.setFill(ARCADE_WHITE);
            ctx.fillText(clapperboard.number(), numberX, numberY);
            ctx.fillText(clapperboard.text(), textX, numberY);
        });
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

    private static final Rectangle2D MARQUEE_BOUNDS = new Rectangle2D(60, 88, 132, 60);
    private static final int BULB_COUNT = 96;
    private static final int ACTIVE_BULBS_DIST = 16;

    /**
     * 6 of the 96 light bulbs are lightning each frame, shifting counter-clockwise every tick.
     * <p>
     * The bulbs on the left border however are switched off every second frame. This is
     * probably a bug in the original Arcade game.
     * </p>
     */
    public void drawMarquee(long tick, Color onColor, Color offColor) {
        int t = (int) (tick % BULB_COUNT);
        for (int i = 0; i < BULB_COUNT; ++i) { drawBulb(i, offColor); }
        for (int b = 0; b < 6; ++b) {
            drawBulb((t + b * ACTIVE_BULBS_DIST) % BULB_COUNT, onColor);
        }
        // simulate bug from original Arcade game
        for (int i = 81; i < BULB_COUNT; i += 2) { drawBulb(i, offColor); }
    }

    private void drawBulb(int i, Color bulbColor) {
        double x, y;
        if (i <= 33) { // lower edge left-to-right
            x = MARQUEE_BOUNDS.getMinX() + 4 * i;
            y = MARQUEE_BOUNDS.getMaxY();
        }
        else if (i <= 48) { // right edge bottom-to-top
            x = MARQUEE_BOUNDS.getMaxX();
            y = 4 * (70 - i);
        }
        else if (i <= 81) { // upper edge right-to-left
            x = 4 * (BULB_COUNT - i);
            y = MARQUEE_BOUNDS.getMinY();
        }
        else { // left edge top-to-bottom
            x = MARQUEE_BOUNDS.getMinX();
            y = 4 * (i - 59);
        }
        ctx().setFill(bulbColor);
        ctx().fillRect(scaled(x), scaled(y), scaled(2), scaled(2));
    }
}