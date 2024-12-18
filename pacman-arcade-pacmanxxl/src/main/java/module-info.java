open module de.amr.games.pacman.arcade.pacman_xxl {
    requires de.amr.games.pacman;
    requires de.amr.games.pacman.lib;
    requires org.tinylog.api;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;
    requires de.amr.games.pacman.ui2d;
    requires de.amr.games.pacman.mapeditor;
    requires de.amr.games.pacman.arcade.pacman;

    exports de.amr.games.pacman.arcade.pacman_xxl;
}