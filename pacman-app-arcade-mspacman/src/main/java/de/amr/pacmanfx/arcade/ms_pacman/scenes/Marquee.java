/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.actors.Actor;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;

public class Marquee extends Actor {
    private final TickTimer timer = new TickTimer("Marquee-Timer");
    private final Rectangle2D bounds;
    private final int totalBulbCount;
    private final int brightBulbsCount;
    private final int brightBulbsDistance;
    private Color bulbOnColor = Color.WHITE;
    private Color bulbOffColor = Color.GREEN;

    public Marquee(Rectangle2D bounds, int totalBulbCount, int brightBulbsCount, int brightBulbsDistance) {
        this.bounds = bounds;
        this.totalBulbCount = totalBulbCount;
        this.brightBulbsCount = brightBulbsCount;
        this.brightBulbsDistance = brightBulbsDistance;
    }

    public TickTimer timer() {
        return timer;
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

    public Rectangle2D bounds() {
        return bounds;
    }
}
