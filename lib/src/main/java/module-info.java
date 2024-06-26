/*
 * Copyright (c) 2021-2024 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
open module de.amr.games.pacman.lib {
    requires org.tinylog.api;
    exports de.amr.games.pacman.lib;
    exports de.amr.games.pacman.lib.fsm;
    exports de.amr.games.pacman.lib.graph;
    exports de.amr.games.pacman.lib.timer;
    exports de.amr.games.pacman.lib.tilemap;
}