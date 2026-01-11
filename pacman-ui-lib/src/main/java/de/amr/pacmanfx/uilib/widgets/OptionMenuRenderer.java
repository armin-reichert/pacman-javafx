/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.widgets;

import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.uilib.Ufx.scaleFontBy;

public class OptionMenuRenderer extends BaseRenderer {

    protected float lineSkip = TS(2);

    public OptionMenuRenderer(Canvas canvas) {
        super(canvas);
    }

    public void drawOptionMenu(OptionMenu menu) {
        final OptionMenuStyle style = menu.style();
        final Font scaledTitleFont = scaleFontBy(style.titleFont(), scaling());
        final Font scaledTextFont = scaleFontBy(style.textFont(), scaling());
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

    protected void drawUsageInfo(OptionMenu menu) {
        final Color txtFill = menu.style().hintTextFill();
        final Color valFill = menu.style().entryValueFill();
        final Font font     = Ufx.scaleFontBy(menu.style().textFont(), scaling());

        final double centerX = 0.5 * menu.numTilesX() * TS;
        double y = TS(menu.numTilesY() - 8);

        fillTextCentered("SELECT OPTIONS WITH UP AND DOWN", txtFill, font, centerX, y);
        fillTextCentered("                    UP     DOWN", valFill, font, centerX, y);

        y += lineSkip;
        drawActionText(KeyCode.SPACE, "CHANGE VALUE", txtFill, valFill, font, centerX, y);

        if (menu.action1KeyCode() != null && menu.action1Text() != null) {
            y += lineSkip;
            drawActionText(menu.action1KeyCode(), menu.action1Text(), txtFill, valFill, font, centerX, y);
        }
        if (menu.action2KeyCode() != null && menu.action2Text() != null) {
            y += lineSkip;
            drawActionText(menu.action2KeyCode(), menu.action2Text(), txtFill, valFill, font, centerX, y);
        }
    }

    private void drawActionText(KeyCode keyCode, String actionText, Color normalColor, Color brightColor, Font font, double centerX, double y) {
        final String normalText = "PRESS %s TO %s".formatted(keyCode, actionText);
        final String brightText = "      %s    %s".formatted(keyCode, " ".repeat(actionText.length()));
        fillTextCentered(normalText, normalColor, font, centerX, y);
        fillTextCentered(brightText, brightColor, font, centerX, y);
    }
}
