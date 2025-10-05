/*
 * Copyright (c) 2021-2025 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
module de.amr.pacmanfx.core {

    requires org.tinylog.api;
    requires javafx.base;
    requires java.desktop;

    exports de.amr.pacmanfx.controller;
    exports de.amr.pacmanfx.event;
    exports de.amr.pacmanfx.model;
    exports de.amr.pacmanfx.model.actors;
    exports de.amr.pacmanfx.lib;
    exports de.amr.pacmanfx.lib.fsm;
    exports de.amr.pacmanfx.lib.graph;
    exports de.amr.pacmanfx.lib.nes;
    exports de.amr.pacmanfx.lib.worldmap;
    exports de.amr.pacmanfx.lib.timer;
    exports de.amr.pacmanfx.steering;
    exports de.amr.pacmanfx;
    exports de.amr.pacmanfx.controller.test;
}