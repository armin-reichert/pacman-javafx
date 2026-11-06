module pacman.javafx.pacman.app {
    requires de.amr.basics;
    requires de.amr.basics.ui;
    requires de.amr.pacmanfx.core;
    requires de.amr.pacmanfx.engine;
    requires de.amr.pacmanfx.ui;
    requires de.amr.pacmanfx.uilib;
    requires javafx.base;
    requires javafx.graphics;
    requires org.tinylog.api;

    exports de.amr.pacmanfx.app.config;
    exports de.amr.pacmanfx.app.runtime;
}