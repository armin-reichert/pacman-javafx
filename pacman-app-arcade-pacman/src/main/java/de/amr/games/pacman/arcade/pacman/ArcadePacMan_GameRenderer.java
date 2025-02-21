/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.ui2d.GameRenderer;
import de.amr.games.pacman.ui2d.assets.AssetStorage;
import de.amr.games.pacman.ui2d.assets.GameSpriteSheet;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.arcade.pacman.ArcadePacMan_SpriteSheet.EMPTY_MAZE_SPRITE;
import static de.amr.games.pacman.arcade.pacman.ArcadePacMan_SpriteSheet.FULL_MAZE_SPRITE;
import static de.amr.games.pacman.lib.Globals.TS;

/**
 * @author Armin Reichert
 */
public class ArcadePacMan_GameRenderer implements GameRenderer {

    private static final Vector2f MESSAGE_POSITION = new Vector2f(14 * TS, 21 * TS);

    private final AssetStorage assets;
    private final ArcadePacMan_SpriteSheet spriteSheet;
    private final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0);
    private final Canvas canvas;
    private boolean mazeHighlighted;
    private boolean blinkingOn;
    private Color bgColor = Color.BLACK;

    public ArcadePacMan_GameRenderer(AssetStorage assets, ArcadePacMan_SpriteSheet spriteSheet, Canvas canvas) {
        this.assets = Globals.assertNotNull(assets);
        this.spriteSheet = Globals.assertNotNull(spriteSheet);
        this.canvas = Globals.assertNotNull(canvas);
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
    public void setMazeHighlighted(boolean highlighted) {
        this.mazeHighlighted = highlighted;
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
        bgColor = Globals.assertNotNull(color);
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
        if (mazeHighlighted) {
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
            drawActorSprite(bonus.actor(), spriteSheet().bonusSymbolSprite(bonus.symbol()));
        } else if (bonus.state() == Bonus.STATE_EATEN) {
            drawActorSprite(bonus.actor(), spriteSheet().bonusValueSprite(bonus.symbol()));
        }
    }
}