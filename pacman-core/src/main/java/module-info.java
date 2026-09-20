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
    exports de.amr.pacmanfx.core.entities.door;
    exports de.amr.pacmanfx.core.entities.house;
    exports de.amr.pacmanfx.core.hud.livescounter;
    exports de.amr.pacmanfx.core.gameplay.hunt;
    exports de.amr.pacmanfx.core.spriteanim;
    exports de.amr.pacmanfx.core.rendering;
    exports de.amr.pacmanfx.core.props.bonuspoints;
    exports de.amr.pacmanfx.core.props.ghostpoints;
    exports de.amr.pacmanfx.core.props.marquee;
    exports de.amr.pacmanfx.core.props.messageview;
    exports de.amr.pacmanfx.core.props.bag;
    exports de.amr.pacmanfx.core.props.clapperboard;
    exports de.amr.pacmanfx.core.props.stork;
    exports de.amr.pacmanfx.core.props.textdisplay;
    exports de.amr.pacmanfx.core.hud.score;
    exports de.amr.pacmanfx.core.hud.levelCounter;
    exports de.amr.pacmanfx.core.entities.pac;
    exports de.amr.pacmanfx.core.entities.bonus;
    exports de.amr.pacmanfx.core.entities.ghost;
}