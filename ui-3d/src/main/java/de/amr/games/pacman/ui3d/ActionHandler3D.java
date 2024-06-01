package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.ui2d.ActionHandler;

public interface ActionHandler3D extends ActionHandler {

    void selectNextPerspective();

    void selectPrevPerspective();

    void toggle2D3D();

    void togglePipVisible();

    void toggleDrawMode();

    void enterMapEditor();

    void quitMapEditor();

}
