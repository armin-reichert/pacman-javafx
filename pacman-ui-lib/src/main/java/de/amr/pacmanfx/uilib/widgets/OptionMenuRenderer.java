/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.widgets;

import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;

public class OptionMenuRenderer extends BaseRenderer {

    public OptionMenuRenderer(Canvas canvas) {
        super(canvas);
    }

    public void drawOptionMenu(OptionMenu menu) {
        final OptionMenuStyle style = menu.style();
        final Font scaledTitleFont = Ufx.scaleFontBy(style.titleFont(), scaling());
        final Font scaledTextFont = Ufx.scaleFontBy(style.textFont(), scaling());
        final double centerX = 0.5 * menu.numTilesX() * TS;

        fillCanvas(style.backgroundFill());
        fillTextCentered(menu.title(), style.titleTextFill(), scaledTitleFont, centerX, 6 * TS);
        int y = 12 * TS;
        for (int i = 0; i < menu.entries().size(); ++i) {
            final OptionMenuEntry<?> entry = menu.entries().get(i);
            if (i == menu.selectedEntryIndex()) {
                final int col = (menu.textColumn() - 2) * TS;
                fillText("-", style.entryTextFill(), scaledTextFont, col, y);
                fillText(">", style.entryTextFill(), scaledTextFont, col + HTS, y);
            }
            fillText(entry.text(), style.entryTextFill(), scaledTextFont, menu.textColumn() * TS, y);
            fillText(entry.formatSelectedValue(),
                entry.enabled() ? style.entryValueFill() : style.entryValueDisabledFill(),
                scaledTextFont,
                menu.valueColumn() * TS, y);
            y += 3 * TS;
        }
        drawUsageInfo(menu);
    }

    protected void drawUsageInfo(OptionMenu menu) {}
}
