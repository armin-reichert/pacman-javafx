/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.ui2d.ActionHandler;

/**
 * @author Armin Reichert
 */
public interface ActionHandler3D extends ActionHandler {

    void selectNextPerspective();

    void selectPrevPerspective();

    void toggle2D3D();

    void toggleDashboard();

    void togglePipVisible();

    void toggleDrawMode();

    void enterMapEditor();

    void quitMapEditor();

}
