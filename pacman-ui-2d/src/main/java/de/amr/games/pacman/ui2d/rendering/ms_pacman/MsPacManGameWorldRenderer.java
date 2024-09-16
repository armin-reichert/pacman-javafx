package de.amr.games.pacman.ui2d.rendering.ms_pacman;

import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.model.mspacman.MsPacManGameModel;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.rendering.SpriteArea;
import de.amr.games.pacman.ui2d.rendering.SpriteGameWorldRenderer;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.lib.Globals.t;

public class MsPacManGameWorldRenderer implements GameWorldRenderer {

    private final MsPacManGameSpriteSheet spriteSheet;
    private final SpriteGameWorldRenderer spriteRenderer = new SpriteGameWorldRenderer();

    private boolean flashMode;
    private boolean blinkingOn;

    public MsPacManGameWorldRenderer(AssetStorage assets) {
        spriteSheet = assets.get("ms_pacman.spritesheet");
        spriteRenderer.setSpriteSheet(spriteSheet);
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
        MsPacManGameModel game = (MsPacManGameModel) context.game();
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
                .forEach(tile -> spriteRenderer.overpaintFood(g, world, tile));
            if (!blinkingOn) {
                world.energizerTiles().forEach(tile -> spriteRenderer.overpaintFood(g, world, tile));
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
        game.bonus().ifPresent(bonus -> spriteRenderer.drawMovingBonus(g, (MovingBonus) bonus));
    }
}