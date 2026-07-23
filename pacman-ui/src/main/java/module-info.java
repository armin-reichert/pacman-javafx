/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/

// module is open to allow access to non-class resources
open module de.amr.pacmanfx.ui {
    requires transitive java.prefs;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.media;
    requires org.tinylog.api;
    requires com.google.gson;
    requires de.amr.basics;
    requires de.amr.pacmanfx.core;
    requires de.amr.pacmanfx.uilib;
    requires de.amr.pacmanfx.mapeditor;
    requires java.desktop;

    exports de.amr.pacmanfx.ui;
    exports de.amr.pacmanfx.ui.action;
    exports de.amr.pacmanfx.ui.views.dashboard;
    exports de.amr.pacmanfx.ui.gamescene.d2;
    exports de.amr.pacmanfx.ui.gamescene.d3;
    exports de.amr.pacmanfx.ui.gamescene.d3.animation;
    exports de.amr.pacmanfx.ui.gamescene.d3.animation.energizer;
    exports de.amr.pacmanfx.ui.gamescene.d3.camera;
    exports de.amr.pacmanfx.ui.gamescene.d3.entities;
    exports de.amr.pacmanfx.ui.input;
    exports de.amr.pacmanfx.ui.views;
    exports de.amr.pacmanfx.ui.sound;
    exports de.amr.pacmanfx.ui.views.playview;
    exports de.amr.pacmanfx.ui.views.startpages;
    exports de.amr.pacmanfx.ui.views.editor;
    exports de.amr.pacmanfx.ui.views.help;
    exports de.amr.pacmanfx.ui.window;
    exports de.amr.pacmanfx.ui.gamescene.common;
    exports de.amr.pacmanfx.ui.action.core;
    exports de.amr.pacmanfx.ui.model;
    exports de.amr.pacmanfx.ui.settings.ui;
    exports de.amr.pacmanfx.ui.settings.world;
    exports de.amr.pacmanfx.game;
    exports de.amr.pacmanfx.ui.vm;
}