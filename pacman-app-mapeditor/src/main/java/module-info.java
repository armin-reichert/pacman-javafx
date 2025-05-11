/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/

// module is open to allow access to non-class resources
open module de.amr.pacmanfx.mapeditor {
    requires javafx.graphics;
    requires javafx.controls;
    requires org.tinylog.api;
    requires de.amr.pacmanfx.core;
    requires de.amr.pacmanfx.uilib;

    exports de.amr.pacmanfx.tilemap.editor.app;
    exports de.amr.pacmanfx.tilemap.editor;
}