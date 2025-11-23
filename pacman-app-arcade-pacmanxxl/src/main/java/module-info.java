/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/

// module is open to allow access to non-class resources
open module de.amr.pacmanfx.arcade.pacman_xxl {
    requires org.tinylog.api;
    requires javafx.controls;
    requires javafx.graphics;
    requires de.amr.pacmanfx.core;
    requires de.amr.pacmanfx.uilib;
    requires de.amr.pacmanfx.ui;
    requires de.amr.pacmanfx.arcade.ms_pacman;
    requires de.amr.pacmanfx.arcade.pacman;
    requires javafx.base;

    exports de.amr.pacmanfx.arcade.pacman_xxl;
}