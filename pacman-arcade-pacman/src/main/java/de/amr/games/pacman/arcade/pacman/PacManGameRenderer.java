/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.ui.GameRenderer;
import de.amr.games.pacman.ui.assets.AssetStorage;
import de.amr.games.pacman.ui.assets.GameSpriteSheet;
import de.amr.games.pacman.ui.assets.WorldMapColoring;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.arcade.pacman.PacManGameSpriteSheet.EMPTY_MAZE_SPRITE;
import static de.amr.games.pacman.arcade.pacman.PacManGameSpriteSheet.FULL_MAZE_SPRITE;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class PacManGameRenderer implements GameRenderer {

    public static final WorldMapColoring WORLDMAP_COLORING = new WorldMapColoring("#000000", "#2121ff", "#fcb5ff", "#febdb4");

    private static final Vector2f MESSAGE_POSITION = new Vector2f(14 * TS, 21 * TS);

    private final AssetStorage assets;
    private final PacManGameSpriteSheet spriteSheet;
    private final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0);
    private final Canvas canvas;
    private boolean flashMode;
    private boolean blinkingOn;
    private Color bgColor = Color.BLACK;

    public PacManGameRenderer(AssetStorage assets, PacManGameSpriteSheet spriteSheet, Canvas canvas) {
        this.assets = checkNotNull(assets);
        this.spriteSheet = checkNotNull(spriteSheet);
        this.canvas = checkNotNull(canvas);
    }

    @Override
    public AssetStorage assets() {
        return assets;
    }

    @Override
    public GameSpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public Canvas canvas() {
        return canvas;
    }

    @Override
    public void setFlashMode(boolean flashMode) {
        this.flashMode = flashMode;
    }

    @Override
    public void setBlinking(boolean blinking) {
        this.blinkingOn = blinking;
    }

    @Override
    public DoubleProperty scalingProperty() {
        return scalingPy;
    }

    @Override
    public Color backgroundColor() {
        return bgColor;
    }

    @Override
    public void setBackgroundColor(Color color) {
        bgColor = checkNotNull(color);
    }

    @Override
    public Vector2f getMessagePosition() {
        return MESSAGE_POSITION;
    }

    @Override
    public void setMessagePosition(Vector2f position) {}

    @Override
    public void drawWorld(GameWorld world, double x, double y) {
        double scaling = scaling();
        ctx().save();
        ctx().scale(scaling, scaling);
        if (flashMode) {
            ctx().drawImage(assets.image("pacman.flashing_maze"), x, y);
        } else {
            if (world.uneatenFoodCount() == 0) {
                drawSpriteUnscaled(EMPTY_MAZE_SPRITE, x, y);
            } else {
                drawSpriteUnscaled(FULL_MAZE_SPRITE, x, y);
                overPaintEatenPellets(world);
                overPaintEnergizers(world, tile -> !blinkingOn || world.hasEatenFoodAt(tile));
            }
        }
        ctx().restore();
    }

    public void drawBonus(Bonus bonus) {
        if (bonus.state() == Bonus.STATE_EDIBLE) {
            drawEntitySprite(bonus.entity(), spriteSheet().bonusSymbolSprite(bonus.symbol()));
        } else if (bonus.state() == Bonus.STATE_EATEN) {
            drawEntitySprite(bonus.entity(), spriteSheet().bonusValueSprite(bonus.symbol()));
        }
    }
}