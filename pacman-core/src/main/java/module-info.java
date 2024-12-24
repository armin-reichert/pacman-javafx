/*
 * Copyright (c) 2021-2024 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
module de.amr.games.pacman {

    requires org.tinylog.api;

    exports de.amr.games.pacman.controller;
    exports de.amr.games.pacman.event;
    exports de.amr.games.pacman.model;
    exports de.amr.games.pacman.model.actors;
    exports de.amr.games.pacman.lib;
    exports de.amr.games.pacman.lib.arcade;
    exports de.amr.games.pacman.lib.fsm;
    exports de.amr.games.pacman.lib.graph;
    exports de.amr.games.pacman.lib.nes;
    exports de.amr.games.pacman.lib.tilemap;
    exports de.amr.games.pacman.lib.timer;
    exports de.amr.games.pacman.steering;
}