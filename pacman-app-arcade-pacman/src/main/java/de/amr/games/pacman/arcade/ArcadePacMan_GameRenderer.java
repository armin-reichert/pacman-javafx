/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.ui._2d.GameRenderer;
import de.amr.games.pacman.ui._2d.GameSpriteSheet;
import de.amr.games.pacman.uilib.AssetStorage;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.arcade.ArcadePacMan_SpriteSheet.EMPTY_MAZE_SPRITE;
import static de.amr.games.pacman.arcade.ArcadePacMan_SpriteSheet.FULL_MAZE_SPRITE;
import static de.amr.games.pacman.Globals.TS;
import static de.amr.games.pacman.Globals.assertNotNull;

/**
 * @author Armin Reichert
 */
public class ArcadePacMan_GameRenderer implements GameRenderer {

    private static final Vector2f MESSAGE_POSITION = new Vector2f(14 * TS, 21 * TS);

    private final AssetStorage assets;
    private final ArcadePacMan_SpriteSheet spriteSheet;
    private final FloatProperty scalingPy = new SimpleFloatProperty(1.0f);
    private final Canvas canvas;
    private boolean mazeHighlighted;
    private boolean blinkingOn;
    private Color bgColor = Color.BLACK;

    public ArcadePacMan_GameRenderer(AssetStorage assets, ArcadePacMan_SpriteSheet spriteSheet, Canvas canvas) {
        this.assets = assertNotNull(assets);
        this.spriteSheet = assertNotNull(spriteSheet);
        this.canvas = assertNotNull(canvas);
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
    public FloatProperty scalingProperty() {
        return scalingPy;
    }

    @Override
    public Color backgroundColor() {
        return bgColor;
    }

    @Override
    public void setBackgroundColor(Color color) {
        bgColor = assertNotNull(color);
    }

    @Override
    public Vector2f getMessagePosition() {
        return MESSAGE_POSITION;
    }

    @Override
    public void setMessagePosition(Vector2f position) {}

    @Override
    public void drawGameLevel(GameLevel level, double x, double y) {
        double scaling = scaling();
        ctx().save();
        ctx().scale(scaling, scaling);
        if (mazeHighlighted) {
            ctx().drawImage(assets.image("pacman.flashing_maze"), x, y);
        } else {
            if (level.uneatenFoodCount() == 0) {
                drawSpriteUnscaled(EMPTY_MAZE_SPRITE, x, y);
            } else {
                drawSpriteUnscaled(FULL_MAZE_SPRITE, x, y);
                overPaintEatenPellets(level);
                overPaintEnergizers(level, tile -> !blinkingOn || level.hasEatenFoodAt(tile));
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