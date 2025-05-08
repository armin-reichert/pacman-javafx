package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.ui.Globals;

import static de.amr.games.pacman.Globals.TS;
import static de.amr.games.pacman.Globals.game;

public class TengenMsPacMan_PiPScene extends TengenMsPacMan_PlayScene2D {

    public TengenMsPacMan_PiPScene() {
        canvas.widthProperty().unbind();
        canvas.heightProperty().unbind();
        viewPortHeightProperty().bind(canvas.heightProperty());
        viewPortWidthProperty().bind(canvas.widthProperty());
    }

    @Override
    public void update() {}

    @Override
    public void draw() {
        double aspect = sizeInPx().x() / sizeInPx().y();
        canvas.setWidth(Globals.PY_PIP_HEIGHT.doubleValue() * aspect);
        canvas.setHeight(Globals.PY_PIP_HEIGHT.doubleValue());
        setScaling(canvas.getHeight() / (sizeInPx().y() + 3 * TS));
        gr.setScaling(scaling());
        gr.fillCanvas(backgroundColor());
        game().level().ifPresent(level -> {
            gr.ctx().save();
            gr.ctx().translate(scaled(TS), 0);
            drawSceneContent();
            gr.ctx().restore();
        });
    }
}
