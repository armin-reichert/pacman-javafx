/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.lib.timer.TickTimer;
import javafx.geometry.Rectangle2D;

public class Marquee {
    private final TickTimer timer = new TickTimer("Marquee-Timer");
    private final Rectangle2D bounds;
    private final int totalBulbCount;
    private final int brightBulbsCount;
    private final int brightBulbsDistance;

    public Marquee(Rectangle2D bounds, int totalBulbCount, int brightBulbsCount, int brightBulbsDistance) {
        this.bounds = bounds;
        this.totalBulbCount = totalBulbCount;
        this.brightBulbsCount = brightBulbsCount;
        this.brightBulbsDistance = brightBulbsDistance;
    }

    public TickTimer timer() {
        return timer;
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

    public Rectangle2D bounds() {
        return bounds;
    }
}
