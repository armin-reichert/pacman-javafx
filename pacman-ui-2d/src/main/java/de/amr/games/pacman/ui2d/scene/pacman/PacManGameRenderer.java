/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.pacman;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class PacManGameRenderer implements GameRenderer {

    private final AssetStorage assets;
    private final GameSpriteSheet spriteSheet;
    private final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0);
    private final Image flashingMazeImage;
    private boolean flashMode;
    private boolean blinkingOn;
    private Canvas canvas;
    private Color bgColor = Color.BLACK;

    public PacManGameRenderer(AssetStorage assets) {
        this.assets = checkNotNull(assets);
        spriteSheet = assets.get("pacman.spritesheet");
        flashingMazeImage = assets.image("pacman.flashing_maze");
    }

    @Override
    public GameRenderer copy() {
        return new PacManGameRenderer(assets);
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
    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
        canvas.getGraphicsContext2D().setImageSmoothing(true);
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
    public void setBlinkingOn(boolean blinkingOn) {
        this.blinkingOn = blinkingOn;
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
    public void update(GameModel game) {}

    @Override
    public void drawWorld(GameContext context, GameWorld world, double x, double y) {
        double scaling = scaling();
        ctx().save();
        ctx().scale(scaling, scaling);
        if (flashMode) {
            if (blinkingOn) {
                ctx().drawImage(flashingMazeImage, x, y);
            } else {
                drawSpriteUnscaled(PacManGameSpriteSheet.EMPTY_MAZE_SPRITE, x, y);
            }
        } else {
            drawSpriteUnscaled(PacManGameSpriteSheet.FULL_MAZE_SPRITE, x, y);
            overPaintEatenPellets(world);
            overPaintEnergizers(world, tile -> !blinkingOn || world.hasEatenFoodAt(tile));
        }
        ctx().restore();
        context.game().bonus().ifPresent(bonus -> drawStaticBonus(spriteSheet, bonus));
    }

    private void drawStaticBonus(GameSpriteSheet spriteSheet, Bonus bonus) {
        if (bonus.state() == Bonus.STATE_EDIBLE) {
            drawSprite(bonus.entity(), spriteSheet.bonusSymbolSprite(bonus.symbol()));
        } else if (bonus.state() == Bonus.STATE_EATEN) {
            drawSprite(bonus.entity(), spriteSheet.bonusValueSprite(bonus.symbol()));
        }
    }
}