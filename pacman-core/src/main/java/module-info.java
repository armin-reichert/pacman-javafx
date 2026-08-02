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
    exports de.amr.pacmanfx.core.model.entities;
    exports de.amr.pacmanfx.core.model.world;
    exports de.amr.pacmanfx.core.model.test;
    exports de.amr.pacmanfx.core.steering;
    exports de.amr.pacmanfx.core;
    exports de.amr.pacmanfx.core.model.level;
    exports de.amr.pacmanfx.core.model.score;
    exports de.amr.pacmanfx.core.gameplay;
    exports de.amr.pacmanfx.core.gamestate;
    exports de.amr.pacmanfx.core.model.rules;
    exports de.amr.pacmanfx.core.ecs.systems.ghost;
    exports de.amr.pacmanfx.core.ecs.systems.marquee;
    exports de.amr.pacmanfx.core.ecs.systems.pac;
    exports de.amr.pacmanfx.core.ecs.systems.common;
    exports de.amr.pacmanfx.core.ecs.systems.spriteanim;
    exports de.amr.pacmanfx.core.ecs.systems.world;
    exports de.amr.pacmanfx.core.ecs.systems.bonus;
    exports de.amr.pacmanfx.core.event.base;
    exports de.amr.pacmanfx.core.event.pac;
    exports de.amr.pacmanfx.core.event.ghost;
    exports de.amr.pacmanfx.core.event.bonus;
    exports de.amr.pacmanfx.core.event.gameplay;
    exports de.amr.pacmanfx.core.model.entities.pac;
    exports de.amr.pacmanfx.core.model.entities.bonus;
    exports de.amr.pacmanfx.core.model.entities.ghost;
    exports de.amr.pacmanfx.core.model.entities.marquee;
    exports de.amr.pacmanfx.core.model.world.map;
    exports de.amr.pacmanfx.core.model.world.house;
    exports de.amr.pacmanfx.core.model.world.obstacle;
    exports de.amr.pacmanfx.core.ecs;
    exports de.amr.pacmanfx.core.ecs.components;
    exports de.amr.pacmanfx.core.ecs.systems;
}