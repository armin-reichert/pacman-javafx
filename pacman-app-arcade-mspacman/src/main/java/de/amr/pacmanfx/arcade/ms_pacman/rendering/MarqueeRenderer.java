/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.core.model.entities.marquee.*;
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
        final MarqueeRunnerComp runner = marquee.runner();
        final MarqueeLayoutComp layout = marquee.layout();
        final MarqueeVisualComp visualComp = marquee.visualization();

        final MarqueeArea area = layout.computeArea(marquee.pos());
        final MarqueeCorners corners = layout.corners();

        final int n = layout.numBulbs();
        final long tick = runner.tickTimer().tickCount();

        final Color offColor = Color.valueOf(visualComp.bulbOffColor());
        final Color onColor = Color.valueOf(visualComp.bulbOnColor());

        drawDarkBulbs(offColor, n, area, corners, layout.bulbSize());
        drawBrightBulbs(onColor, n, layout.brightBulbsCount(), (int) (tick % n), area, corners, layout.bulbSize(), layout.brightBulbsDistance());
    }

    private void drawDarkBulbs(Color color, int n, MarqueeArea area, MarqueeCorners corners, float bulbSize) {
        ctx.setFill(color);
        for (int index = 0; index < n; ++index) {
            drawBulb(area, corners, bulbSize, index);
        }
    }

    private void drawBrightBulbs(Color color, int n, int count, int firstIndex, MarqueeArea area, MarqueeCorners corners,
                                 float bulbSize, int bulbDist) {
        ctx.setFill(color);
        for (int i = 0; i < count; ++i) {
            final int index = (firstIndex + i * bulbDist) % n;
            // Simulate "broken bulbs on left side" bug from original Arcade game
            final boolean broken = index >= corners.nw() && (index - corners.nw()) % 2 == 0;
            if (!broken) {
                drawBulb(area, corners, bulbSize, index);
            }
        }
    }

    private void drawBulb(MarqueeArea a, MarqueeCorners corners, float bs, int index) {

        // Example: bh=35, bv=15

        // 82                   48
        //
        //
        //
        //
        // 0 1 2 ...            34

        double x, y;

        if (index < corners.se()) { // [0;se}: lower edge left-to-right: 0..
            x = a.minX() + index * bs;
            y = a.maxY();
        }
        else if (index < corners.ne()) { // [se;ne): right edge bottom-to-top
            int d = index - corners.se();
            x = a.maxX();
            y = a.maxY() - d * bs;
        }
        else if (index < corners.nw()) { // [ne;nw): upper edge right-to-left
            int d = index - corners.ne();
            x = a.maxX() - d * bs;
            y = a.minY();
        }
        else { // left edge top-to-bottom
            int d = index - corners.nw();
            x = a.minX();
            y = a.minY() + d * bs;
        }

        ctx.fillRect(scaled(x), scaled(y), scaled(0.5 * bs), scaled(0.5 * bs));
    }
}
