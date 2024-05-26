/*
 * Copyright (c) 2021-2024 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
open module de.amr.games.pacman {
    // module is defined as "open" to give access to non-class resources (map data) via class loader

    requires org.tinylog.api;
    requires de.amr.games.pacman.lib;

    exports de.amr.games.pacman.controller;
    exports de.amr.games.pacman.event;
    exports de.amr.games.pacman.model;
    exports de.amr.games.pacman.model.actors;
    exports de.amr.games.pacman.model.world;
    exports de.amr.games.pacman.steering;
}