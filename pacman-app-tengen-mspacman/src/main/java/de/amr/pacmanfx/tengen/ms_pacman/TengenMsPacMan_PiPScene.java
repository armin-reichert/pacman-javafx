package de.amr.pacmanfx.tengen.ms_pacman;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.PacManGames_Env.PY_PIP_HEIGHT;

public class TengenMsPacMan_PiPScene extends TengenMsPacMan_PlayScene2D {

    public TengenMsPacMan_PiPScene() {
        canvas().widthProperty().unbind();
        canvas().heightProperty().unbind();
        viewPortHeightProperty().bind(canvas().heightProperty());
        viewPortWidthProperty().bind(canvas().widthProperty());
    }

    @Override
    public void update() {}

    @Override
    public void draw() {
        //TODO use binding
        double aspect = sizeInPx().x() / sizeInPx().y();
        canvas().setWidth(PY_PIP_HEIGHT.doubleValue() * aspect);
        canvas().setHeight(PY_PIP_HEIGHT.doubleValue());
        setScaling(canvas().getHeight() / (sizeInPx().y() + 3 * TS));
        gr().setScaling(scaling());
        gr().fillCanvas(backgroundColor());
        drawSceneContent();
    }
}
