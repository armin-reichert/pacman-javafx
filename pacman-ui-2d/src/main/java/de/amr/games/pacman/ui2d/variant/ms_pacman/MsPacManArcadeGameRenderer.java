/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.variant.ms_pacman;

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
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.lib.Globals.t;
import static de.amr.games.pacman.ui2d.rendering.GameWorldRenderer.imageArea;

/**
 * @author Armin Reichert
 */
public class MsPacManArcadeGameRenderer implements GameWorldRenderer {

    private final ObjectProperty<Color> backgroundColorPy = new SimpleObjectProperty<>(Color.BLACK);
    private final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0);
    private final Image flashingMazesImage;
    private ImageArea mapWithFoodSprite;
    private ImageArea mapWithoutFoodSprite;
    private ImageArea mapFlashingSprite;
    private boolean flashMode;
    private boolean blinkingOn;

    public MsPacManArcadeGameRenderer(AssetStorage assets) {
        flashingMazesImage = assets.get("ms_pacman.flashing_mazes");
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
    public void selectMap(WorldMap worldMap, int mapNumber, GameSpriteSheet spriteSheet) {
        mapWithFoodSprite    = imageArea(spriteSheet.sourceImage(), 0, (mapNumber - 1) * 248, 226, 248);
        mapWithoutFoodSprite = imageArea(spriteSheet.sourceImage(), 228, (mapNumber - 1) * 248, 226, 248);
        mapFlashingSprite    = imageArea(flashingMazesImage, 0, (mapNumber - 1) * 248, 226, 248);
    }

    @Override
    public void drawWorld(GraphicsContext g, GameSpriteSheet spriteSheet, GameContext context, GameWorld world) {
        double originX = 0, originY = t(3);
        if (flashMode) {
            if (blinkingOn) {
                drawSubImageScaled(g, mapFlashingSprite.source(), mapFlashingSprite.area(), originX - 3, originY); // WTF
            } else {
                drawSubImageScaled(g, mapWithoutFoodSprite.source(), mapWithoutFoodSprite.area(), originX, originY);
            }
        } else {
            g.save();
            g.scale(scalingPy.get(), scalingPy.get());
            drawSpriteUnscaled(g, spriteSheet, mapWithFoodSprite.area(), originX, originY);
            overPaintEatenPellets(g, world);
            overPaintEnergizers(g, world, tile -> !blinkingOn || world.hasEatenFoodAt(tile));
            g.restore();
        }
        context.game().bonus().ifPresent(bonus -> drawMovingBonus(g, context.spriteSheet(), (MovingBonus) bonus));
    }
}