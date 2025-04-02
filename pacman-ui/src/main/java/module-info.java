/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
open module de.amr.games.pacman.ui {
    // module is open to allow access to non-class resources

    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.media;
    requires org.tinylog.api;
    requires de.amr.games.pacman;
    requires de.amr.games.pacman.tilemap;
    requires de.amr.games.pacman.tilemap.editor.app;
    requires de.amr.games.pacman.uilib;

    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    // add icon pack modules
    requires org.kordamp.ikonli.fontawesome5;

    exports de.amr.games.pacman.ui.dashboard;
    exports de.amr.games.pacman.ui.input;
    exports de.amr.games.pacman.ui.sound;
    exports de.amr.games.pacman.ui._2d;
    exports de.amr.games.pacman.ui._3d;
    exports de.amr.games.pacman.ui._3d.animation;
    exports de.amr.games.pacman.ui._3d.dashboard;
    exports de.amr.games.pacman.ui._3d.level;
    exports de.amr.games.pacman.ui._3d.scene3d;
    exports de.amr.games.pacman.ui;
}