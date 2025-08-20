/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.model.actors.Actor;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;

import java.util.BitSet;

import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesColor;

public class Marquee extends Actor {
    private static final int NUM_BULBS = 96;

    private final DoubleProperty scaling = new SimpleDoubleProperty(1.0);
    private final BitSet bulbState = new BitSet(NUM_BULBS);
    private long marqueeProgress;

    public DoubleProperty scalingProperty() {
        return scaling;
    }

    public void update(long tick) {
        if (tick % 4 == 0) {
            marqueeProgress += 2;
            bulbState.clear();
            for (int b = 0; b < 6; ++b) {
                bulbState.set((int) (b * 16 + marqueeProgress) % NUM_BULBS);
            }
        }
    }

    public void draw(GraphicsContext ctx) {
        double xMin = x(), xMax = xMin + 132, yMin = y(), yMax = yMin + 60;
        for (int i = 0; i < NUM_BULBS; ++i) {
            ctx.setFill(bulbState.get(i) ? nesColor(0x20) : nesColor(0x15));
            if (i <= 33) { // lower border left-to-right
                drawBulb(ctx, xMin + 4 * i, yMax);
            } else if (i <= 48) { // right border bottom-to-top
                drawBulb(ctx, xMax, yMax - 4 * (i - 33));
            } else if (i <= 81) { // upper border right-to-left
                drawBulb(ctx, xMax - 4 * (i - 48), yMin);
            } else { // left border top-to-bottom
                drawBulb(ctx, xMin, yMin + 4 * (i - 81));
            }
        }
    }

    private void drawBulb(GraphicsContext ctx, double x, double y) {
        double s = scaling.get();
        ctx.fillRect(s * x, s * y, s * 2, s * 2);
    }
}
