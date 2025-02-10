/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
open module de.amr.games.pacman.arcade.ms_pacman {
    // module is open to give access to non-class resources

    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;

    requires org.tinylog.api;

    requires de.amr.games.pacman;
    requires de.amr.games.pacman.tilemap;
    requires de.amr.games.pacman.ui;
    requires de.amr.games.pacman.uilib;

    exports de.amr.games.pacman.arcade.ms_pacman;
}