/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/

// module is open to allow access to non-class resources
open module de.amr.pacmanfx.ui {
    requires transitive java.prefs;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.media;
    requires org.tinylog.api;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires de.amr.pacmanfx.core;
    requires de.amr.pacmanfx.uilib;
    requires de.amr.pacmanfx.mapeditor;

    exports de.amr.pacmanfx.ui.dashboard;
    exports de.amr.pacmanfx.ui.input;
    exports de.amr.pacmanfx.ui.layout;
    exports de.amr.pacmanfx.ui.sound;
    exports de.amr.pacmanfx.ui._2d;
    exports de.amr.pacmanfx.ui._3d;
    exports de.amr.pacmanfx.ui;
    exports de.amr.pacmanfx.ui.api;
    exports de.amr.pacmanfx.ui.action;
}