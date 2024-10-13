/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.pacman;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.Globals.t;

/**
 * @author Armin Reichert
 */
public class PacManGameRenderer implements GameWorldRenderer {

    private final AssetStorage assets;
    private final ObjectProperty<Color> backgroundColorPy = new SimpleObjectProperty<>(Color.BLACK);
    private final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0);
    private final Image flashingMazeImage;
    private boolean flashMode;
    private boolean blinkingOn;
    private Canvas canvas;

    public PacManGameRenderer(AssetStorage assets) {
        this.assets = checkNotNull(assets);
        flashingMazeImage = assets.image("pacman.flashing_maze");
    }

    @Override
    public GameWorldRenderer copy() {
        return new PacManGameRenderer(assets);
    }

    @Override
    public AssetStorage assets() {
        return assets;
    }

    @Override
    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
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
    public ObjectProperty<Color> backgroundColorProperty() {
        return backgroundColorPy;
    }

    @Override
    public void configure(GameModel game, GameSpriteSheet spriteSheet) {}

    @Override
    public void drawWorld(GameSpriteSheet spriteSheet, GameContext context, GameWorld world) {
        double originX = 0, originY = t(3);
        double scaling = scaling();
        ctx().save();
        ctx().scale(scaling, scaling);
        if (flashMode) {
            if (blinkingOn) {
                ctx().drawImage(flashingMazeImage, originX, originY);
            } else {
                drawSpriteUnscaled(spriteSheet, PacManGameSpriteSheet.EMPTY_MAZE_SPRITE, originX, originY);
            }
        } else {
            drawSpriteUnscaled(spriteSheet, PacManGameSpriteSheet.FULL_MAZE_SPRITE, originX, originY);
            overPaintEatenPellets(world);
            overPaintEnergizers(world, tile -> !blinkingOn || world.hasEatenFoodAt(tile));
        }
        ctx().restore();
        context.game().bonus().ifPresent(bonus -> drawStaticBonus(spriteSheet, bonus));
    }

    private void drawStaticBonus(GameSpriteSheet spriteSheet, Bonus bonus) {
        if (bonus.state() == Bonus.STATE_EDIBLE) {
            drawSprite(bonus.entity(), spriteSheet, spriteSheet.bonusSymbolSprite(bonus.symbol()));
        } else if (bonus.state() == Bonus.STATE_EATEN) {
            drawSprite(bonus.entity(), spriteSheet, spriteSheet.bonusValueSprite(bonus.symbol()));
        }
    }
}