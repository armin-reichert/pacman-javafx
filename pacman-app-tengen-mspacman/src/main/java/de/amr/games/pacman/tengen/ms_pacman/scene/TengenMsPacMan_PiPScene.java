package de.amr.games.pacman.tengen.ms_pacman.scene;

import de.amr.games.pacman.ui._2d.GlobalProperties2d;
import javafx.scene.canvas.Canvas;

import static de.amr.games.pacman.Globals.TS;

public class TengenMsPacMan_PiPScene extends TengenMsPacMan_PlayScene2D {

    private final Canvas canvas;

    public TengenMsPacMan_PiPScene() {
        canvas = new Canvas();
        viewPortHeightProperty().bind(canvas.heightProperty());
        viewPortWidthProperty().bind(canvas.widthProperty());
    }

    @Override
    public void update() {
    }

    @Override
    public Canvas canvas() {
        return canvas;
    }

    @Override
    public void draw() {
        double aspect = sizeInPx().x() / sizeInPx().y();
        canvas.setWidth(GlobalProperties2d.PY_PIP_HEIGHT.doubleValue() * aspect);
        canvas.setHeight(GlobalProperties2d.PY_PIP_HEIGHT.doubleValue());
        setScaling(canvas.getHeight() / (sizeInPx().y() + 3 * TS));
        gr.setScaling(scaling());
        gr.clearCanvas();
        context.game().level().ifPresent(level -> {
            gr.ctx().save();
            gr.ctx().translate(scaled(TS), 0);
            drawSceneContent();
            gr.ctx().restore();
        });
    }
}
