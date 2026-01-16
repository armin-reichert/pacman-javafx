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

/**
 * Base renderer for {@link OptionMenu}.
 * Draws title, entries with selection indicators, and usage hints using pre-defined string constants.
 * Subclasses can override {@link #drawUsageInfo(OptionMenu)} for variant-specific layout.
 */
public class OptionMenuRenderer extends BaseRenderer {

    public static final String SELECT_OPTIONS_WITH_UP_AND_DOWN     = "SELECT OPTIONS WITH UP AND DOWN";
    public static final String SELECT_OPTIONS_WITH_UP_DOWN_OVERLAY = "                    UP     DOWN";

    public static final String PRESS_KEY         = "PRESS %s TO %s";
    public static final String PRESS_KEY_OVERLAY = "      %s    %s";

    public static final String CHANGE_VALUE = "CHANGE VALUE";

    protected float lineSkip = TS(2.5);

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

        float y = 12 * TS;
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
            y += lineSkip;
        }

        drawUsageInfo(menu);
    }

    protected void drawUsageInfo(OptionMenu menu) {
        final Color normalColor = menu.style().usageTextFill();
        final Color brightColor = menu.style().entryValueFill();
        final Font font = Ufx.scaleFontBy(menu.style().textFont(), scaling());

        final double centerX = 0.5 * menu.numTilesX() * TS;
        double y = TS(menu.numTilesY() - 8);

        fillTextCentered(SELECT_OPTIONS_WITH_UP_AND_DOWN, normalColor, font, centerX, y);
        fillTextCentered(SELECT_OPTIONS_WITH_UP_DOWN_OVERLAY, brightColor, font, centerX, y);

        y += lineSkip;
        drawActionText(KeyCode.SPACE, CHANGE_VALUE, normalColor, brightColor, font, centerX, y);

        for (int num = 1; num <= OptionMenu.NUM_CLIENT_ACTIONS; ++num) {
            if (menu.actionKeyCode(num) != null && menu.actionText(num) != null) {
                y += lineSkip;
                drawActionText(menu.actionKeyCode(num), menu.actionText(num), normalColor, brightColor, font, centerX, y);
            }
        }
    }

    private void drawActionText(KeyCode keyCode, String actionText, Color normalColor, Color brightColor, Font font, double centerX, double y) {
        final String normalText = PRESS_KEY.formatted(keyCode, actionText);
        final String brightText = PRESS_KEY_OVERLAY.formatted(keyCode, " ".repeat(actionText.length()));
        fillTextCentered(normalText, normalColor, font, centerX, y);
        fillTextCentered(brightText, brightColor, font, centerX, y);
    }
}
