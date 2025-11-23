/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/

// module is open to allow access to non-class resources
open module de.amr.pacmanfx.arcade.ms_pacman {
    requires javafx.controls;
    requires javafx.graphics;
    requires org.tinylog.api;
    requires de.amr.pacmanfx.core;
    requires de.amr.pacmanfx.uilib;
    requires de.amr.pacmanfx.ui;
    requires de.amr.pacmanfx.arcade.pacman;

    exports de.amr.pacmanfx.arcade.ms_pacman;
    exports de.amr.pacmanfx.arcade.ms_pacman.rendering;
    exports de.amr.pacmanfx.arcade.ms_pacman.scenes;
    exports de.amr.pacmanfx.arcade.ms_pacman.actors;
}