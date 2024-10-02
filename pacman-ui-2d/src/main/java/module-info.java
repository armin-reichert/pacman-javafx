/*
 * Copyright (c) 2021-2024 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
open module de.amr.games.pacman.ui2d {
    // module is open to allow access to (non-class) resources

    requires org.tinylog.api;
    requires de.amr.games.pacman;
    requires de.amr.games.pacman.lib;
    requires de.amr.games.pacman.mapeditor;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;

    exports de.amr.games.pacman.ui2d;
    exports de.amr.games.pacman.ui2d.dashboard;
    exports de.amr.games.pacman.ui2d.page;
    exports de.amr.games.pacman.ui2d.rendering;
    exports de.amr.games.pacman.ui2d.variant.ms_pacman;
    exports de.amr.games.pacman.ui2d.variant.pacman;
    exports de.amr.games.pacman.ui2d.variant.pacman_xxl;
    exports de.amr.games.pacman.ui2d.variant.tengen;
    exports de.amr.games.pacman.ui2d.scene;
    exports de.amr.games.pacman.ui2d.sound;
    exports de.amr.games.pacman.ui2d.util;
}