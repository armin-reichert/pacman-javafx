package de.amr.pacmanfx.uilib.rendering;

import javafx.scene.canvas.Canvas;

public abstract class DebugInfoRenderer extends BaseCanvasRenderer {

    public DebugInfoRenderer(Canvas canvas) {
        super(canvas);
    }

    public abstract void drawDebugInfo();
}
