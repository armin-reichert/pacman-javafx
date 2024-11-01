/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.util.NES_Controller;

public class InfoBox_NES_Controller extends InfoBox {

    public void init(GameContext context) {
        super.init(context);
        NES_Controller controller = NES_Controller.DEFAULT_CONTROLLER; // for now
        labeledValue("Button B", controller.b().getDisplayText());
        labeledValue("Button A", controller.a().getDisplayText());
        labeledValue("SELECT",   controller.select().getDisplayText());
        labeledValue("START",    controller.start().getDisplayText());
        labeledValue("UP",       controller.up().getDisplayText());
        labeledValue("DOWN",     controller.down().getDisplayText());
        labeledValue("LEFT",     controller.left().getDisplayText());
        labeledValue("RIGHT",    controller.right().getDisplayText());
    }
}