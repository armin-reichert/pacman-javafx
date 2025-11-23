/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/

// module is open to allow access to non-class resources
open module de.amr.pacmanfx.tengen.ms_pacman {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;
    requires org.tinylog.api;
    requires de.amr.pacmanfx.core;
    requires de.amr.pacmanfx.uilib;
    requires de.amr.pacmanfx.ui;
    requires java.desktop;

    exports de.amr.pacmanfx.tengen.ms_pacman;
    exports de.amr.pacmanfx.tengen.ms_pacman.app;
    exports de.amr.pacmanfx.tengen.ms_pacman.model;
    exports de.amr.pacmanfx.tengen.ms_pacman.rendering;
    exports de.amr.pacmanfx.tengen.ms_pacman.scenes;
}