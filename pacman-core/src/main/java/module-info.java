/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */

module de.amr.pacmanfx.core {

    requires de.amr.basics;
    requires org.tinylog.api;
    requires javafx.base;
    requires java.desktop;
    requires java.security.jgss;

    exports de.amr.pacmanfx.core.event;
    exports de.amr.pacmanfx.core.model;
    exports de.amr.pacmanfx.core.model.actors;
    exports de.amr.pacmanfx.core.model.world;
    exports de.amr.pacmanfx.core.model.test;
    exports de.amr.pacmanfx.core.steering;
    exports de.amr.pacmanfx.core;
    exports de.amr.pacmanfx.core.model.level;
    exports de.amr.pacmanfx.core.score;
    exports de.amr.pacmanfx.core.gameplay;
    exports de.amr.pacmanfx.core.state;
    exports de.amr.pacmanfx.core.rules;
    exports de.amr.pacmanfx.core.model.component;
    exports de.amr.pacmanfx.core.model.component.common;
    exports de.amr.pacmanfx.core.model.component.ghost;
    exports de.amr.pacmanfx.core.model.component.pac;
    exports de.amr.pacmanfx.core.model.component.world;
    exports de.amr.pacmanfx.core.model.component.spriteanim;
    exports de.amr.pacmanfx.core.model.systems.ghost;
    exports de.amr.pacmanfx.core.model.systems.pac;
    exports de.amr.pacmanfx.core.model.systems.common;
    exports de.amr.pacmanfx.core.model.systems.spriteanim;
    exports de.amr.pacmanfx.core.model.systems.world;
    exports de.amr.pacmanfx.core.model.systems.bonus;
    exports de.amr.pacmanfx.core.model.component.bonus;
}