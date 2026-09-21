/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */

module de.amr.pacmanfx.core {

    requires de.amr.basics;
    requires org.tinylog.api;
    requires javafx.base;
    requires javafx.graphics;

    exports de.amr.pacmanfx.core.event;
    exports de.amr.pacmanfx.core.model;
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
    exports de.amr.pacmanfx.core.entities.world.door;
    exports de.amr.pacmanfx.core.entities.world.house;
    exports de.amr.pacmanfx.core.entities.hud.livescounter;
    exports de.amr.pacmanfx.core.gameplay.hunt;
    exports de.amr.pacmanfx.core.spriteanim;
    exports de.amr.pacmanfx.core.rendering;
    exports de.amr.pacmanfx.core.entities.props.bonuspoints;
    exports de.amr.pacmanfx.core.entities.props.ghostpoints;
    exports de.amr.pacmanfx.core.entities.props.messageview;
    exports de.amr.pacmanfx.core.entities.props.clapperboard;
    exports de.amr.pacmanfx.core.entities.props.stork;
    exports de.amr.pacmanfx.core.entities.hud.score;
    exports de.amr.pacmanfx.core.entities.hud.levelCounter;
    exports de.amr.pacmanfx.core.entities.actor.pac;
    exports de.amr.pacmanfx.core.entities.actor.bonus;
    exports de.amr.pacmanfx.core.entities.actor.ghost;
    exports de.amr.pacmanfx.core.entities.world;
    exports de.amr.pacmanfx.core.entities.props.imagedisplay;
    exports de.amr.pacmanfx.core.entities.props.textdisplay;
}