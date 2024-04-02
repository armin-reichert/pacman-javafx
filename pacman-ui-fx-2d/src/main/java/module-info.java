/*
 * Copyright (c) 2021-2023 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
open module de.amr.games.pacman.ui.fx {

    // module is open to allow access to resources using class loader

    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.media;
    requires org.tinylog.api;
    requires de.amr.games.pacman;


    exports de.amr.games.pacman.ui.fx;
    exports de.amr.games.pacman.ui.fx.input;
    exports de.amr.games.pacman.ui.fx.util;
    exports de.amr.games.pacman.ui.fx.rendering2d;
    exports de.amr.games.pacman.ui.fx.scene2d;
    exports de.amr.games.pacman.ui.fx.page;
}