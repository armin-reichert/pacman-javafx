/*
 * Copyright (c) 2021-2024 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
open module de.amr.games.pacman.ui2d {
    // module is open to allow access to (non-class) resources by 3D UI

    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;

    requires org.tinylog.api;

    requires de.amr.games.pacman;
    requires de.amr.games.pacman.mapeditor;

    exports de.amr.games.pacman.ui2d;
    exports de.amr.games.pacman.ui2d.action;
    exports de.amr.games.pacman.ui2d.assets;
    exports de.amr.games.pacman.ui2d.dashboard;
    exports de.amr.games.pacman.ui2d.input;
    exports de.amr.games.pacman.ui2d.lib;
    exports de.amr.games.pacman.ui2d.page;
    exports de.amr.games.pacman.ui2d.scene;
}