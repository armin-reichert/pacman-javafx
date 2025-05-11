/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/

module de.amr.pacmanfx.allgames {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;
    requires org.tinylog.api;
    requires de.amr.pacmanfx.core;
    requires de.amr.pacmanfx.uilib;
    requires de.amr.pacmanfx.ui;
    requires de.amr.games.pacman.arcade.ms_pacman;
    requires de.amr.games.pacman.arcade.pacman;
    requires de.amr.games.pacman.arcade.pacman_xxl;
    requires de.amr.games.pacman.tengen.ms_pacman;

    exports de.amr.pacmanfx.allgames;
}