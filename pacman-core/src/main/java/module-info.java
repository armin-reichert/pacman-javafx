/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */

module de.amr.pacmanfx.core {

    requires de.amr.basics;
    requires org.tinylog.api;
    requires javafx.base;
    requires java.desktop;

    exports de.amr.pacmanfx.core.event;
    exports de.amr.pacmanfx.core.model;
    exports de.amr.pacmanfx.core.model.actors;
    exports de.amr.pacmanfx.core.model.world;
    exports de.amr.pacmanfx.core.model.test;
    exports de.amr.pacmanfx.core.steering;
    exports de.amr.pacmanfx.core;
    exports de.amr.pacmanfx.core.flow;
    exports de.amr.pacmanfx.core.model.level;
    exports de.amr.pacmanfx.core.score;
    exports de.amr.pacmanfx.core.model.lives;
    exports de.amr.pacmanfx.core.gameplay;
    exports de.amr.pacmanfx.core.state;
}