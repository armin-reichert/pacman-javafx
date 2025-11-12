/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.math.Vector2f;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public interface Renderer {

    GraphicsContext ctx();

    DoubleProperty scalingProperty();

    double scaling();

    void setScaling(double value);

    default double scaled(double value) { return scaling() * value; }

    ObjectProperty<Color> backgroundColorProperty();

    void setBackgroundColor(Color color);

    Color backgroundColor();

    boolean imageSmoothing();

    void setImageSmoothing(boolean b);

    /**
     * Draws a sprite (region inside sprite sheet) unscaled at the given position.
     *
     * @param sprite      the sprite to draw
     * @param x           x-coordinate of left-upper corner
     * @param y           y-coordinate of left-upper corner
     * @param scaled      tells is the destination rectangle's position and size is scaled using the current scaling value
     */
    void drawSprite(RectShort sprite, double x, double y, boolean scaled);

    /**
     * Draws the sprite centered over the given position. The target position is scaled using the current scaling value.
     *
     * @param center position over which sprite gets drawn
     * @param sprite the actor sprite
     */
    void drawSpriteCentered(Vector2f center, RectShort sprite);

    /**
     * Draws the sprite centered over the given position. The target position is scaled using the current scaling value.
     *
     * @param centerX x-position over which sprite gets drawn
     * @param centerY y-position over which sprite gets drawn
     * @param sprite the actor sprite
     */
    void drawSpriteCentered(double centerX, double centerY, RectShort sprite);
}