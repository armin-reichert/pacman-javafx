/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.uilib.widgets.OptionMenu;
import de.amr.pacmanfx.uilib.widgets.OptionMenuRenderer;
import de.amr.pacmanfx.uilib.widgets.OptionMenuStyle;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.TS;

public class PacManXXL_OptionMenuRenderer extends OptionMenuRenderer {

    public PacManXXL_OptionMenuRenderer(Canvas canvas) {
        super(canvas);
    }

    @Override
    protected void drawUsageInfo(OptionMenu menu) {
        final OptionMenuStyle style = menu.style();
        final Color normal = style.hintTextFill(), bright = style.entryValueFill();

        ctx.setFont(style.textFont());

        double y = TS(menu.numTilesY() - 8);
        ctx.setFill(normal);
        ctx.fillText("SELECT OPTIONS WITH", TS(6), y);
        ctx.setFill(bright);
        ctx.fillText("UP", TS(26), y);
        ctx.setFill(normal);
        ctx.fillText("AND", TS(29), y);
        ctx.setFill(bright);
        ctx.fillText("DOWN", TS(33), y);

        y += TS(2);
        ctx.setFill(normal);
        ctx.fillText("PRESS", TS(8), y);
        ctx.setFill(bright);
        ctx.fillText("SPACE", TS(14), y);
        ctx.setFill(normal);
        ctx.fillText("TO CHANGE VALUE", TS(20), y);

        y += TS(2);
        ctx.setFill(normal);
        ctx.fillText("PRESS", TS(10), y);
        ctx.setFill(bright);
        ctx.fillText("E", TS(16), y);
        ctx.setFill(normal);
        ctx.fillText("TO OPEN EDITOR", TS(18), y);

        y += TS(2);
        ctx.setFill(normal);
        ctx.fillText("PRESS", TS(11), y);
        ctx.setFill(bright);
        ctx.fillText("ENTER", TS(17), y);
        ctx.setFill(normal);
        ctx.fillText("TO START", TS(23), y);
    }
}
