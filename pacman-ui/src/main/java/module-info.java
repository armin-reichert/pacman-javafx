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

    exports de.amr.games.pacman.ui2d;
    exports de.amr.games.pacman.ui2d.action;
    exports de.amr.games.pacman.ui2d.dashboard;
    exports de.amr.games.pacman.ui2d.input;
    exports de.amr.games.pacman.ui2d.page;
    exports de.amr.games.pacman.ui2d.rendering;
    exports de.amr.games.pacman.ui2d.scene;
    exports de.amr.games.pacman.ui2d.sound;

    exports de.amr.games.pacman.ui3d;
    exports de.amr.games.pacman.ui3d.animation;
    exports de.amr.games.pacman.ui3d.dashboard;
    exports de.amr.games.pacman.ui3d.level;
    exports de.amr.games.pacman.ui3d.scene3d;
}