module de.amr.games.pacman.ui {
    requires de.amr.games.pacman;
    requires de.amr.games.pacman.lib;
    requires org.tinylog.api;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;

    exports de.amr.games.pacman.ui;
    exports de.amr.games.pacman.ui.input;
    exports de.amr.games.pacman.ui.scene;
    exports de.amr.games.pacman.ui.assets;
    exports de.amr.games.pacman.ui.action;
    exports de.amr.games.pacman.ui.lib;
}