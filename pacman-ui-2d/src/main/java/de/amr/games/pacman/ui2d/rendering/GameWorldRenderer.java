package de.amr.games.pacman.ui2d.rendering;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.ui2d.GameContext;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.t;

public interface GameWorldRenderer {

    DoubleProperty scalingProperty();
    ObjectProperty<Color> backgroundColorProperty();

    SpriteRenderer spriteRenderer();

    void drawWorld(GraphicsContext g, GameContext context, GameWorld world);
    void setFlashMode(boolean on);
    void setBlinkingOn(boolean on);

    default void overpaintFood(GraphicsContext g, GameWorld world, Vector2i tile) {
        double cx = t(tile.x()) + HTS;
        double cy = t(tile.y()) + HTS;
        double s = scalingProperty().get();
        double r = world.isEnergizerPosition(tile) ? 4.5 : 2;
        g.setFill(backgroundColorProperty().get());
        g.fillRect(s * (cx - r), s * (cy - r), s * (2 * r), s * (2 * r));
    }

}
