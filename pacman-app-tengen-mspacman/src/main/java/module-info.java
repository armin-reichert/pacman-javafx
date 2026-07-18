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
    requires com.google.gson;
    requires org.tinylog.api;
    requires de.amr.basics;
    requires de.amr.pacmanfx.core;
    requires de.amr.pacmanfx.uilib;
    requires de.amr.pacmanfx.ui;

    exports de.amr.pacmanfx.tengenmspacman;
    exports de.amr.pacmanfx.tengenmspacman.app;
    exports de.amr.pacmanfx.tengenmspacman.model;
    exports de.amr.pacmanfx.tengenmspacman.rendering;
    exports de.amr.pacmanfx.tengenmspacman.gamescene;
    exports de.amr.pacmanfx.tengenmspacman.flow;
    exports de.amr.pacmanfx.tengenmspacman.config;
    exports de.amr.pacmanfx.tengenmspacman.dashboard;
    exports de.amr.pacmanfx.tengenmspacman.sprites;
}