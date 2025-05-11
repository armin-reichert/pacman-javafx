/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/

// module is open to allow access to non-class resources
open module de.amr.games.pacman.arcade.ms_pacman {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;
    requires org.tinylog.api;
    requires de.amr.pacmanfx.core;
    requires de.amr.pacmanfx.uilib;
    requires de.amr.games.pacman.ui;
    requires de.amr.games.pacman.arcade.pacman;

    exports de.amr.pacmanfx.arcade.ms_pacman;
}