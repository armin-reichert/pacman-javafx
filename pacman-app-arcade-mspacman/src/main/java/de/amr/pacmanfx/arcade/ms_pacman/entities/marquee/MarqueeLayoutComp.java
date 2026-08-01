/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.entities.marquee;


import de.amr.pacmanfx.core.model.GameEntityComponent;

public class MarqueeLayoutComp implements GameEntityComponent {

    private float width;
    private float height;
    private int totalBulbCount;
    private int brightBulbsCount;
    private int brightBulbsDistance;

    public float width() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float height() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public int totalBulbCount() {
        return totalBulbCount;
    }

    public void setTotalBulbCount(int totalBulbCount) {
        this.totalBulbCount = totalBulbCount;
    }

    public int brightBulbsCount() {
        return brightBulbsCount;
    }

    public void setBrightBulbsCount(int brightBulbsCount) {
        this.brightBulbsCount = brightBulbsCount;
    }

    public int brightBulbsDistance() {
        return brightBulbsDistance;
    }

    public void setBrightBulbsDistance(int brightBulbsDistance) {
        this.brightBulbsDistance = brightBulbsDistance;
    }

    @Override
    public void reset() {
    }
}
