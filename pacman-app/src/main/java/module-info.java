module pacman.javafx.pacman.app {
    requires javafx.base;
    requires java.desktop;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.media;

    requires org.tinylog.api;
    requires com.google.gson;
    requires de.amr.pacmanfx.ui;
    requires de.amr.pacmanfx.engine;
    requires de.amr.pacmanfx.core;
    requires de.amr.basics.ui;
    requires de.amr.basics;
    requires de.amr.pacmanfx.uilib;

    exports de.amr.pacmanfx.app.config;
    exports de.amr.pacmanfx.app.runtime;

}