/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.core.model.comp.marquee.MarqueeLayoutComp;
import de.amr.pacmanfx.core.model.comp.marquee.MarqueeRunnerComp;
import de.amr.pacmanfx.core.model.comp.marquee.MarqueeVisualComp;
import de.amr.pacmanfx.core.model.entities.Marquee;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

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

        final long tick = runner.tickTimer().tickCount();
        final Color offColor = Color.valueOf(visualization.bulbOffColor());
        final Color onColor = Color.valueOf(visualization.bulbOnColor());

        ctx.setFill(offColor);
        for (int bulbIndex = 0; bulbIndex < layout.totalBulbCount(); ++bulbIndex) {
            drawMarqueeBulb(marquee, bulbIndex);
        }
        int firstBrightIndex = (int) (tick % layout.totalBulbCount());
        ctx.setFill(onColor);
        for (int i = 0; i < layout.brightBulbsCount(); ++i) {
            drawMarqueeBulb(marquee, (firstBrightIndex + i * layout.brightBulbsDistance()) % layout.totalBulbCount());
        }
        // simulate bug from original Arcade game
        ctx.setFill(offColor);
        for (int bulbIndex = 81; bulbIndex < layout.totalBulbCount(); bulbIndex += 2) {
            drawMarqueeBulb(marquee, bulbIndex);
        }
    }

    private void drawMarqueeBulb(Marquee marquee, int bulbIndex) {
        final MarqueeLayoutComp layout = marquee.layout();

        final double minX = marquee.pos().x(), minY = marquee.pos().y();
        final double maxX = marquee.pos().x() + layout.width();
        final double maxY = marquee.pos().y() + layout.height();

        double x, y;
        if (bulbIndex <= 33) { // lower edge left-to-right
            x = minX + 4 * bulbIndex;
            y = maxY;
        }
        else if (bulbIndex <= 48) { // right edge bottom-to-top
            x = maxX;
            y = 4 * (70 - bulbIndex);
        }
        else if (bulbIndex <= 81) { // upper edge right-to-left
            x = 4 * (layout.totalBulbCount() - bulbIndex);
            y = minY;
        }
        else { // left edge top-to-bottom
            x = minX;
            y = 4 * (bulbIndex - 59);
        }
        ctx.fillRect(scaled(x), scaled(y), scaled(2), scaled(2));
    }
}
