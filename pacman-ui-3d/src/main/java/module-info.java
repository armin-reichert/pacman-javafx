/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
open module de.amr.games.pacman.ui3d {
    // module is open to allow access to non-class resources

    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.media;
    requires org.tinylog.api;
    requires de.amr.games.pacman;
    requires de.amr.games.pacman.lib;
    requires de.amr.games.pacman.ui2d;
    requires de.amr.games.pacman.mapeditor;

    exports de.amr.games.pacman.ui3d;
    exports de.amr.games.pacman.ui3d.animation;
    exports de.amr.games.pacman.ui3d.dashboard;
    exports de.amr.games.pacman.ui3d.level;
    exports de.amr.games.pacman.ui3d.model;
    exports de.amr.games.pacman.ui3d.scene.common;
}