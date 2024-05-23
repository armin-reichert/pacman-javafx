/*
 * Copyright (c) 2021-2024 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
module de.amr.games.pacman.tilemap {
    requires javafx.graphics;
    requires javafx.controls;
    requires org.tinylog.api;

    exports de.amr.games.pacman.lib;
    exports de.amr.games.pacman.tilemap;
    exports de.amr.games.pacman.tilemap.editor;
}