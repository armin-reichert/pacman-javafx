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
        final Color textFill = menu.style().hintTextFill();
        final Color valueFill = menu.style().entryValueFill();

        ctx.setFont(menu.style().textFont());

        double y = TS(menu.numTilesY() - 8);
        drawText("SELECT OPTIONS WITH", textFill, TS(6), y);
        drawText("UP", valueFill, TS(26), y);
        drawText("AND", textFill, TS(29), y);
        drawText("DOWN", valueFill, TS(33), y);

        y += LINE_SKIP;
        drawText("PRESS", textFill, TS(8), y);
        drawText("SPACE", valueFill, TS(14), y);
        drawText("TO CHANGE VALUE", textFill, TS(20), y);

        y += LINE_SKIP;
        drawText("PRESS", textFill, TS(10), y);
        drawText("E", valueFill, TS(16), y);
        drawText("TO OPEN EDITOR", textFill, TS(18), y);

        y += LINE_SKIP;
        drawText("PRESS", textFill, TS(11), y);
        drawText("ENTER", valueFill, TS(17), y);
        drawText("TO START", textFill, TS(23), y);
    }
}