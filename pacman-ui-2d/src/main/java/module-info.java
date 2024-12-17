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
    requires de.amr.games.pacman.lib;
    requires de.amr.games.pacman.mapeditor;
    requires de.amr.games.pacman.ui;
    requires de.amr.games.pacman.tengen.ms_pacman;

    exports de.amr.games.pacman.ui2d;
    exports de.amr.games.pacman.ui2d.dashboard;
    exports de.amr.games.pacman.ui2d.page;
}