/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_RED;

public class CopyrightRenderer extends BaseRenderer {

    private final Image logo;

    public CopyrightRenderer(Canvas canvas, Image logo) {
        super(canvas);
        this.logo = logo;
    }

    public void drawCopyright(double x, double y) {
        ctx.drawImage(logo, scaled(x), scaled(y + 2), scaled(TS(4) - 2), scaled(TS(4)));
        ctx.setFont(arcadeFont8());
        ctx.setFill(ARCADE_RED);
        ctx.fillText("©", scaled(x + TS(5)), scaled(y + TS(2)) + 2);
        ctx.fillText("MIDWAY MFG CO", scaled(x + TS(7)), scaled(y + TS(2)));
        ctx.fillText("1980/1981", scaled(x + TS(8)), scaled(y + TS(4)));

    }
}
