/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/

// module is open to allow access to non-class resources
open module de.amr.pacmanfx.tengenmspacman {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;
    requires org.tinylog.api;
    requires de.amr.pacmanfx.core;
    requires de.amr.pacmanfx.uilib;
    requires de.amr.pacmanfx.ui;
    requires java.desktop;

    exports de.amr.pacmanfx.tengenmspacman;
    exports de.amr.pacmanfx.tengenmspacman.app;
    exports de.amr.pacmanfx.tengenmspacman.model;
    exports de.amr.pacmanfx.tengenmspacman.rendering;
    exports de.amr.pacmanfx.tengenmspacman.scenes;
}