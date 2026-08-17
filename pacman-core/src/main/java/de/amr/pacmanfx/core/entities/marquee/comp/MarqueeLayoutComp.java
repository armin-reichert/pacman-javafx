/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.marquee.comp;


import de.amr.pacmanfx.core.ecs.EntityComponent;
import de.amr.pacmanfx.core.ecs.comp.PositionComp;

public class MarqueeLayoutComp implements EntityComponent {

    private int numBulbsHorizontally;
    private int numBulbsVertically;
    private int bulbSize;
    private int brightBulbsCount;
    private int brightBulbsDistance;
    private MarqueeCorners corners;

    public MarqueeArea computeArea(PositionComp pos) {
        return new MarqueeArea(pos.x(), pos.y(), pos.x() + (numBulbsHorizontally - 1) * bulbSize, pos.y() + (numBulbsVertically - 1) * bulbSize);
    }

    public int numBulbs() {
        final int bh = numBulbsHorizontally;
        final int bv = numBulbsVertically;
        return 2 * (bh + bv) - 4;
    }


    public int numBulbsHorizontally() {
        return numBulbsHorizontally;
    }

    public void setNumBulbsHorizontally(int numBulbsHorizontally) {
        this.numBulbsHorizontally = numBulbsHorizontally;
        corners = null;
    }

    public int numBulbsVertically() {
        return numBulbsVertically;
    }

    public void setNumBulbsVertically(int numBulbsVertically) {
        this.numBulbsVertically = numBulbsVertically;
        corners = null;
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

    public MarqueeCorners corners() {
        if (corners == null) {
            corners = computeCornerIndices();
        }
        return corners;
    }

    @Override
    public void reset() {
    }

    private MarqueeCorners computeCornerIndices() {
        final int bh = numBulbsHorizontally;
        final int bv = numBulbsVertically;
        final int sw = 0;
        final int se = sw + bh - 1;
        final int ne = se + bv - 1;
        final int nw = ne + bh - 1;
        return new MarqueeCorners(sw, se, ne, nw);
    }
}
