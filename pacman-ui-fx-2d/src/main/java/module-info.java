/*
 * Copyright (c) 2021-2024 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
open module de.amr.games.pacman.ui.fx {
    // module is open to allow access to (non-class) resources

    requires org.tinylog.api;
    requires de.amr.games.pacman;
    requires de.amr.games.pacman.lib;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;

    exports de.amr.games.pacman.ui.fx;
    exports de.amr.games.pacman.ui.fx.util;
    exports de.amr.games.pacman.ui.fx.rendering2d;
    exports de.amr.games.pacman.ui.fx.scene2d;
    exports de.amr.games.pacman.ui.fx.page;
    exports de.amr.games.pacman.ui.fx.tilemap;
}