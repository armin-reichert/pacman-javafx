/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.lib.timer.TickTimer;
import javafx.geometry.Rectangle2D;

public record Marquee(TickTimer timer, Rectangle2D bounds, int totalBulbCount, int brightBulbsCount,
                      int brightBulbsDistance) {
}
