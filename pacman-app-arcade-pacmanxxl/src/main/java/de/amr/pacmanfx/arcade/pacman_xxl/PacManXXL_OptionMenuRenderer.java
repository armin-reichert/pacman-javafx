/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.uilib.widgets.OptionMenu;
import de.amr.pacmanfx.uilib.widgets.OptionMenuRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.TS;

public class PacManXXL_OptionMenuRenderer extends OptionMenuRenderer {

    public static final float LINE_SKIP = TS(2);

    public PacManXXL_OptionMenuRenderer(Canvas canvas) {
        super(canvas);
    }

    private void drawText(String text, Color fill, double x, double y) {
        ctx.setFill(fill);
        ctx.fillText(text, x, y);
    }

    @Override
    protected void drawUsageInfo(OptionMenu menu) {
        final Color txtFill = menu.style().hintTextFill();
        final Color valFill = menu.style().entryValueFill();

        ctx.setFont(menu.style().textFont());

        double y = TS(menu.numTilesY() - 8);
        drawText("SELECT OPTIONS WITH UP AND DOWN", txtFill, TS(6), y);
        drawText("                    UP     DOWN", valFill, TS(6), y);

        y += LINE_SKIP;
        drawText("PRESS SPACE TO CHANGE VALUE", txtFill, TS(8), y);
        drawText("      SPACE                ", valFill, TS(8), y);

        y += LINE_SKIP;
        drawText("PRESS E TO OPEN EDITOR", txtFill, TS(10), y);
        drawText("      E               ", valFill, TS(10), y);

        y += LINE_SKIP;
        drawText("PRESS ENTER TO START", txtFill, TS(11), y);
        drawText("      ENTER         ", valFill, TS(11), y);
    }
}