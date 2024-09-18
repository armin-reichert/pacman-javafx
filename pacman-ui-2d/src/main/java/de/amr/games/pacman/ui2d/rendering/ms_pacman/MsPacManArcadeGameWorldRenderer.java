/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering.ms_pacman;

import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.SpriteArea;
import de.amr.games.pacman.ui2d.rendering.SpriteRenderer;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.t;

public class MsPacManArcadeGameWorldRenderer implements MsPacManGameWorldRenderer {

    private final MsPacManGameSpriteSheet spriteSheet;
    private final SpriteRenderer spriteRenderer = new SpriteRenderer();

    private boolean flashMode;
    private boolean blinkingOn;

    public MsPacManArcadeGameWorldRenderer(AssetStorage assets) {
        spriteSheet = assets.get("ms_pacman.spritesheet");
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
        MsPacManGame game = (MsPacManGame) context.game();
        double scaling = scalingProperty().get();
        double x = 0, y = t(3);
        if (flashMode) {
            g.save();
            g.scale(scaling, scaling);
            if (blinkingOn) {
                SpriteArea emptyMazeBright = spriteSheet.highlightedMaze(game.currentMapNumber());
                spriteRenderer.drawSubImage(g, spriteSheet.getFlashingMazesImage(), emptyMazeBright, x - 3, y);
            } else {
                spriteRenderer.drawSprite(g, spriteSheet.emptyMaze(game.currentMapNumber()), x, y);
            }
            g.restore();
        } else {
            g.save();
            g.scale(scaling, scaling);
            spriteRenderer.drawSprite(g, spriteSheet.filledMaze(game.currentMapNumber()), x, y);
            g.restore();
            world.map().food().tiles().filter(world::hasEatenFoodAt)
                .forEach(tile -> overpaintFood(g, world, tile));
            if (!blinkingOn) {
                world.energizerTiles().forEach(tile -> overpaintFood(g, world, tile));
            }
        }
        /*
        var msPacManGame = (MsPacManGameModel) game;
        if (msPacManGame.blueMazeBug) {
            // no map image available, use vector renderer
            drawWorld(flashMode, blinkingOn);
        } else {
            int mapNumber = msPacManGame.currentMapNumber();
            spriteRenderer.drawMsPacManWorld(g, game.world(), mapNumber, flashMode, blinkingOn);
        }
        */
        game.bonus().ifPresent(bonus -> drawMovingBonus(g, (MovingBonus) bonus));
    }

    @Override
    public void drawMovingBonus(GraphicsContext g, MovingBonus movingBonus) {
        g.save();
        g.translate(0, movingBonus.elongationY());
        if (movingBonus.state() == Bonus.STATE_EDIBLE) {
            spriteRenderer.drawEntitySprite(g,  movingBonus.entity(), spriteSheet.bonusSymbolSprite(movingBonus.symbol()));
        } else if (movingBonus.state() == Bonus.STATE_EATEN) {
            spriteRenderer.drawEntitySprite(g, movingBonus.entity(), spriteSheet.bonusValueSprite(movingBonus.symbol()));
        }
        g.restore();
    }

    @Override
    public void drawClapperBoard(GraphicsContext g, Font font, Color textColor, ClapperboardAnimation animation, double x, double y)
    {
        double scaling = scalingProperty().get();
        var sprite = animation.currentSprite(spriteSheet.clapperboardSprites());
        if (sprite != null) {
            spriteRenderer.drawSpriteCenteredOverBox(g, sprite, x, y);
            g.setFont(font);
            g.setFill(textColor.darker());
            var numberX = scaling * (x + sprite.width() - 25);
            var numberY = scaling * (y + 18);
            g.setFill(textColor);
            g.fillText(animation.number(), numberX, numberY);
            var textX = scaling * (x + sprite.width());
            g.fillText(animation.text(), textX, numberY);
        }
    }
}