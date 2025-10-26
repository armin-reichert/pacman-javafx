package de.amr.pacmanfx.ui._2d;

import javafx.scene.canvas.Canvas;

public interface CanvasProvider {

    void setCanvas(Canvas canvas);
    Canvas canvas();
}
