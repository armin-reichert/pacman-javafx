/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

/**
 * A rectangular area with float precision to represent sprite sheet regions.
 *
 * @param x left-upper corner x
 * @param y left-upper corner y
 * @param width width of rectangle
 * @param height height of rectangle
 */
public record RectAreaFloat(float x, float y, float width, float height) {

    public static final RectAreaFloat PIXEL = new RectAreaFloat(0, 0, 1, 1);

    public static RectAreaFloat rect(double x, double y, double width, double height) {
        return new RectAreaFloat(x, y, width, height);
    }

    public RectAreaFloat(double x, double y, double width, double height) {
        this((float) x, (float) y, (float) width, (float) height);
    }

    public boolean contains(double x, double y) {
        return this.x <= x && x < this.x + width &&  this.y <= y && y < this.y + height;
    }
}