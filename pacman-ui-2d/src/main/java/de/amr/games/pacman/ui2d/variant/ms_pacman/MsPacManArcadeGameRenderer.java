/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.variant.ms_pacman;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.rendering.RectArea;
import de.amr.games.pacman.ui2d.rendering.SpriteSheetArea;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.t;

/**
 * @author Armin Reichert
 */
public class MsPacManArcadeGameRenderer implements GameWorldRenderer {

    private final ObjectProperty<Color> backgroundColorPy = new SimpleObjectProperty<>(Color.BLACK);
    private final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0);
    private final Image flashingMazesImage;
    private SpriteSheetArea mapWithFoodSprite;
    private SpriteSheetArea mapWithoutFoodSprite;
    private SpriteSheetArea mapFlashingSprite;
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
        mapWithFoodSprite = new SpriteSheetArea(spriteSheet.sourceImage(),
            new RectArea(0, (mapNumber - 1) * 248, 226, 248));
        mapWithoutFoodSprite = new SpriteSheetArea(spriteSheet.sourceImage(),
            new RectArea(228, (mapNumber - 1) * 248, 226, 248));
        mapFlashingSprite = new SpriteSheetArea(flashingMazesImage,
            new RectArea(0, (mapNumber - 1) * 248, 226, 248));
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

    @Override
    public void drawMovingBonus(GraphicsContext g, GameSpriteSheet spriteSheet, MovingBonus bonus) {
        g.save();
        g.translate(0, bonus.elongationY());
        switch (bonus.state()) {
            case Bonus.STATE_EDIBLE -> drawEntitySprite(g, bonus.entity(), spriteSheet, spriteSheet.bonusSymbolSprite(bonus.symbol()));
            case Bonus.STATE_EATEN  -> drawEntitySprite(g, bonus.entity(), spriteSheet, spriteSheet.bonusValueSprite(bonus.symbol()));
            default -> {}
        }
        g.restore();
    }

    @Override
    public void drawClapperBoard(GraphicsContext g, GameSpriteSheet spriteSheet, Font font, Color textColor, ClapperboardAnimation animation, double x, double y) {
        RectArea sprite = animation.currentSprite(spriteSheet.clapperboardSprites());
        if (sprite != RectArea.PIXEL) {
            double numberX = scaled(x + sprite.width() - 25);
            double numberY = scaled(y + 18);
            double textX = scaled(x + sprite.width());
            drawSpriteCenteredOverBox(g, spriteSheet, sprite, x, y);
            g.setFont(font);
            g.setFill(textColor.darker());
            g.setFill(textColor);
            g.fillText(animation.number(), numberX, numberY);
            g.fillText(animation.text(), textX, numberY);
        }
    }
}