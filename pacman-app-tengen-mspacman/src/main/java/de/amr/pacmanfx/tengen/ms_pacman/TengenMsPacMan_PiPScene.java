package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.ui.PacManGamesEnv;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.Globals.game;

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
        canvas.setWidth(PacManGamesEnv.PY_PIP_HEIGHT.doubleValue() * aspect);
        canvas.setHeight(PacManGamesEnv.PY_PIP_HEIGHT.doubleValue());
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
