/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.Renderable;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_RED;

public class CopyrightRenderer extends BaseRenderer {

    public CopyrightRenderer(Canvas canvas) {
        super(canvas);
    }

    @Override
    public void render(Renderable r, long tick) {
        //TODO
    }

    public void drawCopyright(Image logo, double x, double y) {
        final Font arcade8 = Ufx.deriveFont(GlobalAssets.Fonts.ARCADE.font(), scaled(8));
        ctx.drawImage(logo, scaled(x), scaled(y + 2), scaled(tilesPx(4) - 2), scaled(tilesPx(4)));
        ctx.setFont(arcade8);
        ctx.setFill(ARCADE_RED);
        ctx.fillText("©", scaled(x + tilesPx(5)), scaled(y + tilesPx(2)) + 2);
        ctx.fillText("MIDWAY MFG CO", scaled(x + tilesPx(7)), scaled(y + tilesPx(2)));
        ctx.fillText("1980/1981", scaled(x + tilesPx(8)), scaled(y + tilesPx(4)));
    }
}
