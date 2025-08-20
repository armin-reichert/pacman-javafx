package de.amr.pacmanfx.uilib.rendering;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public abstract class DebugInfoRenderer extends BaseRenderer {

    public DebugInfoRenderer(Canvas canvas) {
        super(canvas);
    }

    public abstract void drawDebugInfo();
}
