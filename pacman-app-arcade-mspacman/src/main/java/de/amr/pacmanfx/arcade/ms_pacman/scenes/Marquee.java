/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.lib.TickTimer;
import javafx.scene.paint.Color;

public class Marquee {
    private final TickTimer timer = new TickTimer("Marquee-Timer");
    private final float width;
    private final float height;
    private final int totalBulbCount;
    private final int brightBulbsCount;
    private final int brightBulbsDistance;
    private Color bulbOnColor = Color.WHITE;
    private Color bulbOffColor = Color.GREEN;
    private final int x;
    private final int y;

    public Marquee(int x, int y, float width, float height, int totalBulbCount, int brightBulbsCount, int brightBulbsDistance) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.totalBulbCount = totalBulbCount;
        this.brightBulbsCount = brightBulbsCount;
        this.brightBulbsDistance = brightBulbsDistance;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public TickTimer timer() {
        return timer;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public Color bulbOnColor() {
        return bulbOnColor;
    }

    public void setBulbOnColor(Color bulbOnColor) {
        this.bulbOnColor = bulbOnColor;
    }

    public Color bulbOffColor() {
        return bulbOffColor;
    }

    public void setBulbOffColor(Color bulbOffColor) {
        this.bulbOffColor = bulbOffColor;
    }

    public int totalBulbCount() {
        return totalBulbCount;
    }

    public int brightBulbsCount() {
        return brightBulbsCount;
    }

    public int brightBulbsDistance() {
        return brightBulbsDistance;
    }
}
