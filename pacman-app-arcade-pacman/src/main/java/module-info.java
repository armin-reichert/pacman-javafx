/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
open module de.amr.games.pacman.arcade.pacman {
    requires org.tinylog.api;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;
    requires de.amr.games.pacman;
    requires de.amr.games.pacman.tilemap;
    requires de.amr.games.pacman.ui;

    exports de.amr.games.pacman.arcade.pacman;
    exports de.amr.games.pacman.arcade;
}