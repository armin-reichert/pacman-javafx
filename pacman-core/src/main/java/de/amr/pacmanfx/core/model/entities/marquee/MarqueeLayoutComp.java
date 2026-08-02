/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.entities.marquee;


import de.amr.pacmanfx.core.ecs.GameEntityComponent;

public class MarqueeLayoutComp implements GameEntityComponent {

    private int numBulbsHorizontally;
    private int numBulbsVertically;
    private int bulbSize;
    private int brightBulbsCount;
    private int brightBulbsDistance;

    public int numBulbsHorizontally() {
        return numBulbsHorizontally;
    }

    public void setNumBulbsHorizontally(int numBulbsHorizontally) {
        this.numBulbsHorizontally = numBulbsHorizontally;
    }

    public int numBulbsVertically() {
        return numBulbsVertically;
    }

    public void setNumBulbsVertically(int numBulbsVertically) {
        this.numBulbsVertically = numBulbsVertically;
    }

    public int bulbSize() {
        return bulbSize;
    }

    public void setBulbSize(int bulbSize) {
        this.bulbSize = bulbSize;
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
