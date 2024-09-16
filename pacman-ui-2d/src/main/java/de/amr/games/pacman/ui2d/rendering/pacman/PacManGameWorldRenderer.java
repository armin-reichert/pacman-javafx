/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering.pacman;

import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.rendering.SpriteRenderer;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.lib.Globals.t;

public class PacManGameWorldRenderer implements GameWorldRenderer {

    private final PacManGameSpriteSheet spriteSheet;
    private final SpriteRenderer spriteRenderer = new SpriteRenderer();

    private boolean flashMode;
    private boolean blinkingOn;

    public PacManGameWorldRenderer(AssetStorage assets) {
        spriteSheet = assets.get("pacman.spritesheet");
        spriteRenderer.setSpriteSheet(spriteSheet);
    }

    @Override
    public SpriteRenderer spriteRenderer() {
        return spriteRenderer;
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
        return spriteRenderer.scalingPy;
    }

    @Override
    public ObjectProperty<Color> backgroundColorProperty() {
        return spriteRenderer.backgroundColorPy;
    }

    @Override
    public void drawWorld(GraphicsContext g, GameContext context, GameWorld world) {
        double scaling = scalingProperty().get();
        g.save();
        g.scale(scaling, scaling);
        if (flashMode) {
            if (blinkingOn) {
                // bright maze is in separate image, not in sprite sheet
                g.drawImage(spriteSheet.getFlashingMazeImage(), t(0), t(3));
            } else {
                spriteRenderer.drawSprite(g, spriteSheet.getEmptyMazeSprite(), t(0), t(3));
            }
        } else {
            spriteRenderer.drawSprite(g, spriteSheet.getFullMazeSprite(), t(0), t(3));
            g.restore();
            world.map().food().tiles().filter(world::hasEatenFoodAt)
                .forEach(tile -> overpaintFood(g, world, tile));
            if (!blinkingOn) {
                world.energizerTiles().forEach(tile -> overpaintFood(g, world, tile));
            }
        }
        g.restore();
        context.game().bonus().ifPresent(bonus -> drawStaticBonus(g, bonus));
    }

    private void drawStaticBonus(GraphicsContext g, Bonus bonus)
    {
        if (bonus.state() == Bonus.STATE_EDIBLE) {
            spriteRenderer.drawEntitySprite(g,  bonus.entity(), spriteSheet.bonusSymbolSprite(bonus.symbol()));
        } else if (bonus.state() == Bonus.STATE_EATEN) {
            spriteRenderer.drawEntitySprite(g,  bonus.entity(), spriteSheet.bonusValueSprite(bonus.symbol()));
        }
    }
}