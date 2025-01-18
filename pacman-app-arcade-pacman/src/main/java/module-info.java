/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
open module de.amr.games.pacman.arcade.pacman {
    requires org.tinylog.api;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;
    requires de.amr.games.pacman;
    requires de.amr.games.pacman.mapeditor;
    requires de.amr.games.pacman.ui3d;

    exports de.amr.games.pacman.arcade.pacman;
    exports de.amr.games.pacman.arcade;
}