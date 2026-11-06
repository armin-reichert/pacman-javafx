/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
module de.amr.pacmanfx.engine {
    requires de.amr.basics;
    requires de.amr.basics.ui;
    requires de.amr.pacmanfx.core;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;
    requires org.tinylog.api;

    exports de.amr.pacmanfx.engine;
    exports de.amr.pacmanfx.engine.action;
    exports de.amr.pacmanfx.engine.config;
    exports de.amr.pacmanfx.engine.gamescene;
    exports de.amr.pacmanfx.engine.input;
    exports de.amr.pacmanfx.engine.sound;
}