/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.widgets;

import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.text.TextAlignment;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;

public class OptionMenuRenderer extends BaseRenderer {

    public OptionMenuRenderer(Canvas canvas) {
        super(canvas);
    }

    public void drawOptionMenu(OptionMenu menu) {
        final OptionMenuStyle style = menu.style();

        fillCanvas(style.backgroundFill());

        ctx.save();
        ctx.scale(scaling(), scaling());

        ctx.setFont(style.titleFont());
        ctx.setFill(style.titleTextFill());
        drawCentered(menu.title(), 6 * TS);

        ctx.setFont(style.textFont());
        for (int i = 0; i < menu.entries().size(); ++i) {
            final int y = (12 + 3 * i) * TS;
            final OptionMenuEntry<?> entry = menu.entries().get(i);
            if (i == menu.selectedEntryIndex()) {
                final int col = (menu.textColumn() - 2) * TS;
                ctx.setFill(style.entryTextFill());
                ctx.fillText("-", col, y);
                ctx.fillText(">", col + HTS, y);
            }
            ctx.setFill(style.entryTextFill());
            ctx.fillText(entry.text(), menu.textColumn() * TS, y);
            ctx.setFill(entry.enabled() ? style.entryValueFill() : style.entryValueDisabledFill());
            ctx.fillText(entry.formatSelectedValue(), menu.valueColumn() * TS, y);
        }

        drawUsageInfo(menu);
        ctx.restore();
    }

    protected void drawUsageInfo(OptionMenu menu) {}

    protected void drawCentered(String text, double y) {
        ctx.save();
        ctx.setTextAlign(TextAlignment.CENTER);
        ctx.fillText(text, (ctx.getCanvas().getWidth() * 0.5) / scaling(), y);
        ctx.restore();
    }
}
