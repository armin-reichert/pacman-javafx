/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/

module de.amr.pacmanfx.allgames {
    requires javafx.graphics;
    requires org.tinylog.api;
    requires de.amr.pacmanfx.core;
    requires de.amr.pacmanfx.uilib;
    requires de.amr.pacmanfx.ui;
    requires de.amr.pacmanfx.arcade.ms_pacman;
    requires de.amr.pacmanfx.arcade.pacman;
    requires de.amr.pacmanfx.arcade.pacman_xxl;
    requires de.amr.pacmanfx.tengenmspacman;

    exports de.amr.pacmanfx.allgames.app;
}