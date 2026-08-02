/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.rendering;

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
        final MarqueeVisualComp visualization = marquee.visualization();

        final int n = layout.numBulbs();
        final long tick = runner.tickTimer().tickCount();
        int firstBrightIndex = (int) (tick % n);

        final Color offColor = Color.valueOf(visualization.bulbOffColor());
        final Color onColor = Color.valueOf(visualization.bulbOnColor());

        ctx.setFill(offColor);
        for (int i = 0; i < n; ++i) {
            drawBulb(marquee, layout.corners(), i);
        }

        final MarqueeCorners corners = layout.corners();
        ctx.setFill(onColor);
        for (int i = 0; i < layout.brightBulbsCount(); ++i) {
            final int index = (firstBrightIndex + i * layout.brightBulbsDistance()) % n;
            // Simulate "broken bulbs on left side" bug from original Arcade game
            final boolean broken = index >= corners.nw() && (index - corners.nw()) % 2 == 0;
            if (!broken) {
                drawBulb(marquee, corners, index);
            }
        }
    }

    private void drawBulb(Marquee marquee, MarqueeCorners corners, int index) {
        final MarqueeLayoutComp layout = marquee.layout();

        final int bh = layout.numBulbsHorizontally();
        final int bv = layout.numBulbsVertically();
        final int bs = layout.bulbSize();

        final float minX = marquee.pos().x();
        final float minY = marquee.pos().y();
        final float maxX = minX + (bh - 1) * bs;
        final float maxY = minY + (bv - 1) * bs;

        // Example: bh=35, bv=15

        // 82                   48
        //
        //
        //
        //
        // 0 1 2 ...            34

        double x, y;

        if (index < corners.se()) { // [0;se}: lower edge left-to-right: 0..
            x = minX + index * bs;
            y = maxY;
        }
        else if (index < corners.ne()) { // [se;ne): right edge bottom-to-top
            int d = index - corners.se();
            x = maxX;
            y = maxY - d * bs;
        }
        else if (index < corners.nw()) { // [ne;nw): upper edge right-to-left
            int d = index - corners.ne();
            x = maxX - d * bs;
            y = minY;
        }
        else { // left edge top-to-bottom
            int d = index - corners.nw();
            x = minX;
            y = minY + d * bs;
        }

        ctx.fillRect(scaled(x), scaled(y), scaled(.5*bs), scaled(.5*bs));
    }
}
