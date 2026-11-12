/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/

// module is open to allow access to non-class resources
open module de.amr.pacmanfx.ui {
    requires de.amr.basics;
    requires de.amr.basics.ui;
    requires de.amr.pacmanfx.core;
    requires de.amr.pacmanfx.engine;
    requires de.amr.pacmanfx.mapeditor;
    requires de.amr.pacmanfx.uilib;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;
    requires org.tinylog.api;

    exports de.amr.pacmanfx.ui;
    exports de.amr.pacmanfx.ui.action;
    exports de.amr.pacmanfx.ui.assets;
    exports de.amr.pacmanfx.ui.entities3D.ghost.comp;
    exports de.amr.pacmanfx.ui.entities3D.pac.comp;
    exports de.amr.pacmanfx.ui.entities3D.pac.system;
    exports de.amr.pacmanfx.ui.entities3D.world;
    exports de.amr.pacmanfx.ui.entities3D.world.system;
    exports de.amr.pacmanfx.ui.gamescene.animation;
    exports de.amr.pacmanfx.ui.gamescene.common;
    exports de.amr.pacmanfx.ui.gamescene.view3d;
    exports de.amr.pacmanfx.ui.gamescene.view3d.animation;
    exports de.amr.pacmanfx.ui.gamescene.view3d.animation.energizer;
    exports de.amr.pacmanfx.ui.gamescene.view3d.camera;
    exports de.amr.pacmanfx.ui.gamescene.playscene;
    exports de.amr.pacmanfx.ui.rendering;
    exports de.amr.pacmanfx.ui.settings.ui;
    exports de.amr.pacmanfx.ui.settings.world;
    exports de.amr.pacmanfx.ui.sound;
    exports de.amr.pacmanfx.ui.viewmodel;
    exports de.amr.pacmanfx.ui.views;
    exports de.amr.pacmanfx.ui.views.dashboard;
    exports de.amr.pacmanfx.ui.views.editor;
    exports de.amr.pacmanfx.ui.views.help;
    exports de.amr.pacmanfx.ui.views.miniview;
    exports de.amr.pacmanfx.ui.views.playview;
    exports de.amr.pacmanfx.ui.views.startpages;
    exports de.amr.pacmanfx.ui.window;
    exports de.amr.pacmanfx.ui.entities3D.score;
    exports de.amr.pacmanfx.ui.entities3D.ghost.system;
    exports de.amr.pacmanfx.ui.entities3D.bonus.system;
    exports de.amr.pacmanfx.ui.entities3D.house.system;
    exports de.amr.pacmanfx.ui.systems;
    exports de.amr.pacmanfx.ui.entities3D.livescounter;
}