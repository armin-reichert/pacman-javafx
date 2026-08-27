/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.core.entities.Marquee;
import de.amr.pacmanfx.core.entities.marquee.comp.*;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

/**
 * Renders a marquee with some highlighted bulbs that seem to circle in counter-clockwise order. Imitates the bug from the
 * original Arcade game where bulbs on the left side of the marquee are only working every second tick.
 */
public class MarqueeRenderer extends BaseRenderer {

    public MarqueeRenderer(Canvas canvas) {
        super(canvas);
    }

    /**
     * 6 of the 96 light bulbs are bright in each frame, shifting counter-clockwise every tick.
     * <p>
     * The bulbs on the left border however are switched off every second frame. This is
     * probably a bug in the original Arcade game.
     * </p>
     */
    public void drawMarquee(Marquee marquee) {
        final MarqueeAnimComp runner = marquee.anim();
        final MarqueeLayoutComp layout = marquee.layout();
        final MarqueeVisualComp visualComp = marquee.visualization();

        final MarqueeArea area = layout.computeArea(marquee.pos());
        final MarqueeCorners corners = layout.corners();

        final int numBulbs = layout.numBulbs();
        final int bulbSize = layout.bulbSize();
        final double scaledBulbRadius = scaled(0.5 * bulbSize);

        drawDarkBulbs(
            area,
            corners,
            Color.valueOf(visualComp.bulbOffColor()),
            numBulbs,
            bulbSize,
            scaledBulbRadius);

        final long tick = runner.tickTimer().tickCount();
        drawBrightBulbs(
            area,
            corners,
            Color.valueOf(visualComp.bulbOnColor()),
            numBulbs,
            layout.brightBulbsCount(),
            layout.brightBulbsDistance(),
            (int) (tick % numBulbs),
            bulbSize,
            scaledBulbRadius);
    }

    private void drawDarkBulbs(
        MarqueeArea area,
        MarqueeCorners corners,
        Color color,
        int numBulbs,
        int bulbSize,
        double bulbRadius) {

        ctx.setFill(color);
        for (int index = 0; index < numBulbs; ++index) {
            drawBulb(area, corners, index, bulbSize, bulbRadius);
        }
    }

    private void drawBrightBulbs(
        MarqueeArea area,
        MarqueeCorners corners,
        Color color,
        int numBulbs,
        int brightBulbsCount,
        int brightBulbsDist,
        int firstBrightBulbIndex,
        int bulbSize,
        double bulbRadius) {

        ctx.setFill(color);
        for (int i = 0; i < brightBulbsCount; ++i) {
            final int index = (firstBrightBulbIndex + i * brightBulbsDist) % numBulbs;
            // Simulate "broken bulbs on left side" bug from original Arcade game
            final boolean broken = index >= corners.nw() && (index - corners.nw()) % 2 == 0;
            if (!broken) {
                drawBulb(area, corners, index, bulbSize, bulbRadius);
            }
        }
    }

    // Example: bh=35, bv=15
    // 82                   48
    //
    //
    //
    //
    // 0 1 2 ...            34
    private void drawBulb(MarqueeArea area, MarqueeCorners corners, int bulbIndex, int bulbSize, double scaledRadius) {
        double x, y;
        if (bulbIndex < corners.se()) {
            // [0;se): bottom edge left-to-right
            x = area.minX() + bulbIndex * bulbSize;
            y = area.maxY();
        }
        else if (bulbIndex < corners.ne()) {
            // [se;ne): right edge bottom-to-top
            final int d = bulbIndex - corners.se();
            x = area.maxX();
            y = area.maxY() - d * bulbSize;
        }
        else if (bulbIndex < corners.nw()) {
            // [ne;nw): upper edge right-to-left
            final int d = bulbIndex - corners.ne();
            x = area.maxX() - d * bulbSize;
            y = area.minY();
        }
        else {
            // left edge top-to-bottom
            final int d = bulbIndex - corners.nw();
            x = area.minX();
            y = area.minY() + d * bulbSize;
        }
        ctx.fillRect(scaled(x), scaled(y), scaledRadius, scaledRadius);
    }
}
