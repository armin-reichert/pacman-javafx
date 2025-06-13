/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib;

/**
 * A rectangular area with short integer precision to represent a sprite.
 *
 * @param x left-upper corner x
 * @param y left-upper corner y
 * @param width width of sprite
 * @param height height of sprite
 */
public record Sprite(short x, short y, short width, short height) {

    public static Sprite makeSprite(int x, int y, int width, int height) {
        return new Sprite(x, y, width, height);
    }

    public Sprite(int x, int y, int width, int height) {
        this((short) x, (short) y, (short) width, (short) height);
    }

    public int xMax() { return x + width; }

    public int yMax() { return y + height; }

    public boolean contains(int x, int y) {
        return this.x <= x && x < xMax() &&  this.y <= y && y < yMax();
    }

    public Vector2f center() { return Vector2f.of(x + width * 0.5f, y + height * 0.5f); }
}