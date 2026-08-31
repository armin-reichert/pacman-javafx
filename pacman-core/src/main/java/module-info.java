/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */

module de.amr.pacmanfx.core {

    requires org.tinylog.api;
    requires javafx.base;
    requires javafx.graphics;
    requires de.amr.basics;
    requires java.management;

    exports de.amr.pacmanfx.core.event;
    exports de.amr.pacmanfx.core.model;
    exports de.amr.pacmanfx.core.entities;
    exports de.amr.pacmanfx.core.model.test;
    exports de.amr.pacmanfx.core.steering;
    exports de.amr.pacmanfx.core;
    exports de.amr.pacmanfx.core.level;
    exports de.amr.pacmanfx.core.gameplay;
    exports de.amr.pacmanfx.core.gamestate;
    exports de.amr.pacmanfx.core.rules;
    exports de.amr.pacmanfx.core.event.base;
    exports de.amr.pacmanfx.core.event.pac;
    exports de.amr.pacmanfx.core.event.ghost;
    exports de.amr.pacmanfx.core.event.bonus;
    exports de.amr.pacmanfx.core.event.gameplay;
    exports de.amr.pacmanfx.core.model.world.map;
    exports de.amr.pacmanfx.core.model.world.obstacle;
    exports de.amr.pacmanfx.core.ecs;
    exports de.amr.pacmanfx.core.ecs.comp;
    exports de.amr.pacmanfx.core.ecs.systems;
    exports de.amr.pacmanfx.core.entities.pac.system;
    exports de.amr.pacmanfx.core.entities.bonus.system;
    exports de.amr.pacmanfx.core.entities.bonuspoints.comp;
    exports de.amr.pacmanfx.core.entities.ghost.system;
    exports de.amr.pacmanfx.core.entities.marquee.system;
    exports de.amr.pacmanfx.core.entities.bonus.comp;
    exports de.amr.pacmanfx.core.entities.clapperboard.comp;
    exports de.amr.pacmanfx.core.entities.clapperboard.system;
    exports de.amr.pacmanfx.core.entities.ghost.comp;
    exports de.amr.pacmanfx.core.entities.house.comp;
    exports de.amr.pacmanfx.core.entities.livescounter.comp;
    exports de.amr.pacmanfx.core.entities.marquee.comp;
    exports de.amr.pacmanfx.core.entities.messageview.comp;
    exports de.amr.pacmanfx.core.entities.pac.comp;
    exports de.amr.pacmanfx.core.entities.ghostpoints.comp;
    exports de.amr.pacmanfx.core.entities.levelCounter.comp;
    exports de.amr.pacmanfx.core.entities.levelCounter.system;
    exports de.amr.pacmanfx.core.entities.score.comp;
    exports de.amr.pacmanfx.core.entities.score.system;
    exports de.amr.pacmanfx.core.gameplay.hunt;
    exports de.amr.pacmanfx.core.spriteanim;
}