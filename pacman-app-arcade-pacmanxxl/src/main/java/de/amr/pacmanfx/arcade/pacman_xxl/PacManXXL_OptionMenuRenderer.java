/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.widgets.OptionMenu;
import de.amr.pacmanfx.uilib.widgets.OptionMenuRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.Globals.TS;

public class PacManXXL_OptionMenuRenderer extends OptionMenuRenderer {

    public static final float LINE_SKIP = TS(2);

    public PacManXXL_OptionMenuRenderer(Canvas canvas) {
        super(canvas);
    }

    @Override
    protected void drawUsageInfo(OptionMenu menu) {
        final Color txtFill = menu.style().hintTextFill();
        final Color valFill = menu.style().entryValueFill();
        final Font font     = Ufx.deriveFont(menu.style().textFont(), scaling() * menu.style().textFont().getSize());
        final double centerX = 0.5 * menu.numTilesX() * TS;

        double y = TS(menu.numTilesY() - 8);
        fillTextCentered("SELECT OPTIONS WITH UP AND DOWN", txtFill, font, centerX, y);
        fillTextCentered("                    UP     DOWN", valFill, font, centerX, y);

        y += LINE_SKIP;
        fillTextCentered("PRESS SPACE TO CHANGE VALUE", txtFill, font, centerX, y);
        fillTextCentered("      SPACE                ", valFill, font, centerX, y);

        y += LINE_SKIP;
        fillTextCentered("PRESS E TO OPEN EDITOR", txtFill, font, centerX, y);
        fillTextCentered("      E               ", valFill, font, centerX, y);

        y += LINE_SKIP;
        fillTextCentered("PRESS ENTER TO START", txtFill, font, centerX, y);
        fillTextCentered("      ENTER         ", valFill, font, centerX, y);
    }
}