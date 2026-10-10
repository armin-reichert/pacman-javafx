module de.amr.pacmanfx.engine {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;

    requires org.tinylog.api;

    requires de.amr.basics;
    requires de.amr.basics.ui;
    requires de.amr.pacmanfx.core;
    requires de.amr.pacmanfx.uilib;

    exports de.amr.pacmanfx.engine;
    exports de.amr.pacmanfx.engine.action;
    exports de.amr.pacmanfx.engine.gamescene;
    exports de.amr.pacmanfx.engine.input;
}