/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.entities;

import de.amr.pacmanfx.core.model.GameEntity;
import javafx.scene.paint.Color;

public class Marquee extends GameEntity {

    private final float width;
    private final float height;
    private final int totalBulbCount;
    private final int brightBulbsCount;
    private final int brightBulbsDistance;
    private Color bulbOnColor = Color.WHITE;
    private Color bulbOffColor = Color.GREEN;

    public Marquee(int x, int y, float width, float height, int totalBulbCount, int brightBulbsCount, int brightBulbsDistance) {
        setComponent(MarqueeTimerComp.class, new MarqueeTimerComp());
        pos().set(x, y);
        this.width = width;
        this.height = height;
        this.totalBulbCount = totalBulbCount;
        this.brightBulbsCount = brightBulbsCount;
        this.brightBulbsDistance = brightBulbsDistance;
    }

    public MarqueeTimerComp timer() {
        return requireComponent(MarqueeTimerComp.class);
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
