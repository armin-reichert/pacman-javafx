/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/

// module is open to allow access to non-class resources
open module de.amr.pacmanfx.arcade.pacman {
    requires org.tinylog.api;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;
    requires de.amr.pacmanfx.core;
    requires de.amr.pacmanfx.uilib;
    requires de.amr.pacmanfx.ui;

    exports de.amr.pacmanfx.arcade.pacman;
    exports de.amr.pacmanfx.arcade.pacman.app;
    exports de.amr.pacmanfx.arcade.pacman.model;
    exports de.amr.pacmanfx.arcade.pacman.model.actors;
    exports de.amr.pacmanfx.arcade.pacman.rendering;
    exports de.amr.pacmanfx.arcade.pacman.scenes;
}