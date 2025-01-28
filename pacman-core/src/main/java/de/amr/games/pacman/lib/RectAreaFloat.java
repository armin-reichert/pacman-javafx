/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

/**
 * A rectangular area with float precision.
 *
 * @param x left-upper corner x
 * @param y left-upper corner y
 * @param width width of rectangle
 * @param height height of rectangle
 */
public record RectAreaFloat(float x, float y, float width, float height) {

    public boolean contains(double x, double y) {
        return this.x <= x && x < this.x + width &&  this.y <= y && y < this.y + height;
    }
}