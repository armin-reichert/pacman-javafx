/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering.pacman;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.Globals.t;

/**
 * @author Armin Reichert
 */
public class PacManArcadeGameWorldRenderer implements GameWorldRenderer {

    private final ObjectProperty<Color> backgroundColorPy = new SimpleObjectProperty<>(Color.BLACK);
    private final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0);
    private final AssetStorage assets;
    private final Image flashingMazeImage;
    private boolean flashMode;
    private boolean blinkingOn;

    public PacManArcadeGameWorldRenderer(AssetStorage assets) {
        this.assets = checkNotNull(assets);
        flashingMazeImage = assets.image("pacman.flashing_maze");
    }

    @Override
    public PacManGameSpriteSheet spriteSheet() {
        return assets.get("pacman.spritesheet");
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
    public void selectMap(WorldMap worldMap, int mapNumber) {
        //TODO what?
    }

    @Override
    public void drawWorld(GraphicsContext g, GameContext context, GameWorld world) {
        double originX = 0, originY = t(3);
        double scaling = scaling();
        g.save();
        g.scale(scaling, scaling);
        if (flashMode) {
            if (blinkingOn) {
                g.drawImage(flashingMazeImage, originX, originY);
            } else {
                drawSpriteUnscaled(g, spriteSheet().getEmptyMazeSprite(), originX, originY);
            }
        } else {
            drawSpriteUnscaled(g, spriteSheet().getFullMazeSprite(), originX, originY);
            overPaintEatenPellets(g, world);
            overPaintEnergizers(g, world, tile -> !blinkingOn || world.hasEatenFoodAt(tile));
        }
        g.restore();
        context.game().bonus().ifPresent(bonus -> drawStaticBonus(g, bonus));
    }

    public void drawMidwayCopyright(GraphicsContext g, double x, double y, Color color, Font font) {
        drawText(g, "© 1980 MIDWAY MFG.CO.", color, font, x, y);
    }

    private void drawStaticBonus(GraphicsContext g, Bonus bonus) {
        if (bonus.state() == Bonus.STATE_EDIBLE) {
            drawEntitySprite(g,  bonus.entity(), spriteSheet().bonusSymbolSprite(bonus.symbol()));
        } else if (bonus.state() == Bonus.STATE_EATEN) {
            drawEntitySprite(g,  bonus.entity(), spriteSheet().bonusValueSprite(bonus.symbol()));
        }
    }
}