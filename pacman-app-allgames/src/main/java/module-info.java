/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/

module x.de.amr.pacmanfx.allgames {
    requires javafx.graphics;
    requires de.amr.pacmanfx.core;
    requires de.amr.pacmanfx.uilib;
    requires de.amr.pacmanfx.ui;

    requires arcade.ms_pacman;
    requires x.arcade.pacman;
    requires arcade.pacman_xxl;
    requires tengen.ms_pacman;

    exports de.amr.pacmanfx.allgames.app;
}