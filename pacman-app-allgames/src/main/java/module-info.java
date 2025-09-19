/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/

module pacmanfx.all {
    requires javafx.graphics;
    requires de.amr.pacmanfx.core;
    requires de.amr.pacmanfx.uilib;
    requires de.amr.pacmanfx.ui;

    requires de.amr.pacmanfx.arcade.ms_pacman;
    requires de.amr.pacmanfx.arcade.pacman;
    requires de.amr.pacmanfx.arcade.pacman_xxl;
    requires de.amr.pacmanfx.tengen.ms_pacman;
    requires org.tinylog.api;

    exports de.amr.pacmanfx.allgames.app;
}