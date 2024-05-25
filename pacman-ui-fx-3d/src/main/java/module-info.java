/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
open module mod.pacman_ui_fx_3d {

    // module is open to allow access to resources using class loader

    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.media;
    requires org.tinylog.api;
    requires mod.pacman_core;
    requires mod.pacman_lib;
    requires mod.pacman_ui_fx_2d;

    exports de.amr.games.pacman.ui.fx.v3d;
    exports de.amr.games.pacman.ui.fx.v3d.animation;
    exports de.amr.games.pacman.ui.fx.v3d.dashboard;
    exports de.amr.games.pacman.ui.fx.v3d.entity;
    exports de.amr.games.pacman.ui.fx.v3d.model;
    exports de.amr.games.pacman.ui.fx.v3d.scene3d;
}