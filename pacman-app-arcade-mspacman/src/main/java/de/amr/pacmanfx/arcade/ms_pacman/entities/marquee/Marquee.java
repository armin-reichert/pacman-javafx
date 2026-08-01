/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.entities.marquee;

import de.amr.pacmanfx.core.model.GameEntity;

public class Marquee extends GameEntity {

    private final float width;
    private final float height;
    private final int totalBulbCount;
    private final int brightBulbsCount;
    private final int brightBulbsDistance;

    public Marquee(int x, int y, float width, float height, int totalBulbCount, int brightBulbsCount, int brightBulbsDistance) {
        setComponent(MarqueeTimerComp.class, new MarqueeTimerComp());
        setComponent(MarqueeVisualization.class, new MarqueeVisualization());

        pos().set(x, y);
        this.width = width;
        this.height = height;
        this.totalBulbCount = totalBulbCount;
        this.brightBulbsCount = brightBulbsCount;
        this.brightBulbsDistance = brightBulbsDistance;
    }

    public MarqueeTimerComp runner() {
        return requireComponent(MarqueeTimerComp.class);
    }

    public MarqueeVisualization visualization() {
        return requireComponent(MarqueeVisualization.class);
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
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
