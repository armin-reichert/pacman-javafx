/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/

// module is open to allow access to non-class resources
open module de.amr.pacmanfx.mapeditor {
    requires javafx.graphics;
    requires javafx.controls;
    requires org.tinylog.api;
    requires de.amr.pacmanfx.core;
    requires de.amr.pacmanfx.uilib;

    exports de.amr.pacmanfx.mapeditor;
    exports de.amr.pacmanfx.mapeditor.actions;
    exports de.amr.pacmanfx.mapeditor.app;
    exports de.amr.pacmanfx.mapeditor.palette;
    exports de.amr.pacmanfx.mapeditor.properties;
    exports de.amr.pacmanfx.mapeditor.rendering;
}