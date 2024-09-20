/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering.ms_pacman;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.RectangularArea;
import de.amr.games.pacman.ui2d.rendering.SpriteRenderer;
import de.amr.games.pacman.ui2d.rendering.SpriteSheetArea;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.t;

/**
 * @author Armin Reichert
 */
public class MsPacManArcadeGameWorldRenderer extends SpriteRenderer implements MsPacManGameWorldRenderer {

    private final MsPacManGameSpriteSheet spriteSheet;

    private SpriteSheetArea mapWithFoodSprite;
    private SpriteSheetArea mapWithoutFoodSprite;
    private SpriteSheetArea mapFlashingSprite;
    private boolean flashMode;
    private boolean blinkingOn;

    public MsPacManArcadeGameWorldRenderer(AssetStorage assets) {
        spriteSheet = assets.get("ms_pacman.spritesheet");
        setSpriteSheet(spriteSheet);
    }

    @Override
    public SpriteRenderer spriteRenderer() {
        return this;
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
        mapWithFoodSprite = new SpriteSheetArea(spriteSheet.sourceImage(),
            new RectangularArea(0, (mapNumber - 1) * 248, 226, 248));
        mapWithoutFoodSprite = new SpriteSheetArea(spriteSheet.sourceImage(),
            new RectangularArea(228, (mapNumber - 1) * 248, 226, 248));
        mapFlashingSprite = new SpriteSheetArea(spriteSheet.getFlashingMazesImage(),
            new RectangularArea(0, (mapNumber - 1) * 248, 226, 248));
    }

    @Override
    public void drawWorld(GraphicsContext g, GameContext context, GameWorld world) {
        double x = 0, y = t(3);
        if (flashMode) {
            if (blinkingOn) {
                drawSubImageScaled(g, mapFlashingSprite.source(), mapFlashingSprite.area(), x - 3, y); // WTF
            } else {
                drawSubImageScaled(g, mapWithoutFoodSprite.source(), mapWithoutFoodSprite.area(), x, y);
            }
        } else {
            drawSpriteScaled(g, mapWithFoodSprite.area(), x, y);
            world.map().food().tiles().filter(world::hasEatenFoodAt).forEach(tile -> overPaintFood(g, world, tile));
            if (!blinkingOn) {
                world.energizerTiles().forEach(tile -> overPaintFood(g, world, tile));
            }
        }
        context.game().bonus().ifPresent(bonus -> drawMovingBonus(g, (MovingBonus) bonus));
    }

    @Override
    public void drawMovingBonus(GraphicsContext g, MovingBonus bonus) {
        g.save();
        g.translate(0, bonus.elongationY());
        switch (bonus.state()) {
            case Bonus.STATE_EDIBLE -> drawEntitySprite(g, bonus.entity(), spriteSheet.bonusSymbolSprite(bonus.symbol()));
            case Bonus.STATE_EATEN  -> drawEntitySprite(g, bonus.entity(), spriteSheet.bonusValueSprite(bonus.symbol()));
            default -> {}
        }
        g.restore();
    }

    @Override
    public void drawClapperBoard(GraphicsContext g, Font font, Color textColor, ClapperboardAnimation animation, double x, double y) {
        var sprite = animation.currentSprite(spriteSheet.clapperboardSprites());
        if (sprite != RectangularArea.EMPTY) {
            drawSpriteCenteredOverBox(g, sprite, x, y);
            g.setFont(font);
            g.setFill(textColor.darker());
            var numberX = scaled(x + sprite.width() - 25);
            var numberY = scaled(y + 18);
            g.setFill(textColor);
            g.fillText(animation.number(), numberX, numberY);
            var textX = scaled(x + sprite.width());
            g.fillText(animation.text(), textX, numberY);
        }
    }
}