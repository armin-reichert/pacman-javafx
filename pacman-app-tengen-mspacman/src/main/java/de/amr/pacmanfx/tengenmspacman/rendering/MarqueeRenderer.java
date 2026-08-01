/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.core.model.comp.marquee.MarqueeLayoutComp;
import de.amr.pacmanfx.core.model.comp.marquee.MarqueeRunnerComp;
import de.amr.pacmanfx.core.model.comp.marquee.MarqueeVisualComp;
import de.amr.pacmanfx.core.model.entities.Marquee;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

/**
 * Renders a marquee with some highlighted bulbs that seem to circle in counter-clockwise order. Imitates the bug from the
 * original Arcade game where bulbs on the left side of the marquee are only working every second tick.
 *
 * TODO: this is a 1:1 copy of the renderer in the Arcade subproject. should be reused
 */
public class MarqueeRenderer extends BaseRenderer {

    // Index 0 is the lower-left (south-west) corner, then index follows border in counter-clockwise order
    record CornerIndices(int sw, int se, int ne, int nw) {}

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

        final long tick = runner.tickTimer().tickCount();
        final int n = numBulbs(layout);

        final CornerIndices corners = computeCornerIndices(marquee);

        int firstBrightIndex = (int) (tick % n);

        final Color offColor = Color.valueOf(visualization.bulbOffColor());
        final Color onColor = Color.valueOf(visualization.bulbOnColor());

        ctx.setFill(offColor);
        for (int i = 0; i < n; ++i) {
            drawBulb(marquee, corners, i);
        }

        ctx.setFill(onColor);
        for (int i = 0; i < layout.brightBulbsCount(); ++i) {
            final int index = (firstBrightIndex + i * layout.brightBulbsDistance()) % n;
            // Simulate "broken bulbs on left side" bug from orginal Arcade game
            final boolean broken = index >= corners.nw() && (index - corners.nw()) % 2 == 0;
            if (!broken) {
                drawBulb(marquee, corners, index);
            }
        }
    }

    private int numBulbs(MarqueeLayoutComp layout) {
        final int bh = layout.numBulbsHorizontally();
        final int bv = layout.numBulbsVertically();
        return 2 * (bh + bv) - 4;
    }

    private CornerIndices computeCornerIndices(Marquee marquee) {
        final MarqueeLayoutComp layout = marquee.layout();
        final int bh = layout.numBulbsHorizontally();
        final int bv = layout.numBulbsVertically();
        final int sw = 0;
        final int se = sw + bh - 1;
        final int ne = se + bv - 1;
        final int nw = ne + bh - 1;
        return new CornerIndices(sw, se, ne, nw);
    }

    private void drawBulb(Marquee marquee, CornerIndices corners, int index) {
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
            y = minY + d * 4;
        }

        ctx.fillRect(scaled(x), scaled(y), scaled(2), scaled(2));
    }
}
