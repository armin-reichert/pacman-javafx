package de.amr.games.pacman.ui2d.rendering;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.ui2d.GameContext;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.t;

public interface GameWorldRenderer {

    DoubleProperty scalingProperty();

    default double scaled(double factor) {
        return scalingProperty().get() * factor;
    }

    ObjectProperty<Color> backgroundColorProperty();

    SpriteRenderer spriteRenderer();

    void drawWorld(GraphicsContext g, GameContext context, GameWorld world);
    void setFlashMode(boolean on);
    void setBlinkingOn(boolean on);

    default void overpaintFood(GraphicsContext g, GameWorld world, Vector2i tile) {
        double cx = t(tile.x()) + HTS;
        double cy = t(tile.y()) + HTS;
        double r = world.isEnergizerPosition(tile) ? 4.5 : 2;
        g.setFill(backgroundColorProperty().get());
        g.fillRect(scaled(cx - r), scaled(cy - r), scaled(2 * r), scaled(2 * r));
    }

    /**
     * Draws a text with the given style at the given (unscaled) position.
     *
     * @param g     graphics context
     * @param text  text
     * @param color text color
     * @param font  text font
     * @param x     unscaled x-position
     * @param y     unscaled y-position (baseline)
     */
    default void drawText(GraphicsContext g, String text, Color color, Font font, double x, double y) {
        g.setFont(font);
        g.setFill(color);
        g.fillText(text, scaled(x), scaled(y));
    }
}