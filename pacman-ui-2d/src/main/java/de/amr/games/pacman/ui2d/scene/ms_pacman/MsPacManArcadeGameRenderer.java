/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.rendering.ImageArea;
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
import static de.amr.games.pacman.ui2d.rendering.GameWorldRenderer.imageArea;

/**
 * @author Armin Reichert
 */
public class MsPacManArcadeGameRenderer implements GameWorldRenderer {

    private final AssetStorage assets;
    private final ObjectProperty<Color> backgroundColorPy = new SimpleObjectProperty<>(Color.BLACK);
    private final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0);
    private final Image flashingMazesImage;
    private ImageArea mapWithFoodSprite;
    private ImageArea mapWithoutFoodSprite;
    private ImageArea mapFlashingSprite;
    private boolean flashMode;
    private boolean blinkingOn;
    private Canvas canvas;

    public MsPacManArcadeGameRenderer(AssetStorage assets) {
        this.assets = checkNotNull(assets);
        flashingMazesImage = assets.get("ms_pacman.flashing_mazes");
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
    public void selectMapSprite(WorldMap worldMap, int mapNumber, GameSpriteSheet spriteSheet) {
        mapWithFoodSprite    = imageArea(spriteSheet.sourceImage(), 0, (mapNumber - 1) * 248, 226, 248);
        mapWithoutFoodSprite = imageArea(spriteSheet.sourceImage(), 228, (mapNumber - 1) * 248, 226, 248);
        mapFlashingSprite    = imageArea(flashingMazesImage, 0, (mapNumber - 1) * 248, 226, 248);
    }

    @Override
    public void drawWorld(GameSpriteSheet spriteSheet, GameContext context, GameWorld world) {
        double originX = 0, originY = t(3);
        if (flashMode) {
            if (blinkingOn) {
                drawSubImageScaled(mapFlashingSprite.source(), mapFlashingSprite.area(), originX - 3, originY); // WTF
            } else {
                drawSubImageScaled(mapWithoutFoodSprite.source(), mapWithoutFoodSprite.area(), originX, originY);
            }
        } else {
            ctx().save();
            ctx().scale(scalingPy.get(), scalingPy.get());
            drawSpriteUnscaled(spriteSheet, mapWithFoodSprite.area(), originX, originY);
            overPaintEatenPellets(world);
            overPaintEnergizers(world, tile -> !blinkingOn || world.hasEatenFoodAt(tile));
            ctx().restore();
        }
        context.game().bonus().ifPresent(bonus -> drawMovingBonus(context.spriteSheet(), (MovingBonus) bonus));
    }
}