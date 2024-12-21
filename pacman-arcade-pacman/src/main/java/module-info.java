open module de.amr.games.pacman.arcade.pacman {
    requires de.amr.games.pacman;
    requires org.tinylog.api;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;
    requires de.amr.games.pacman.ui2d;
    requires de.amr.games.pacman.ui3d;

    exports de.amr.games.pacman.arcade.pacman;
    exports de.amr.games.pacman.arcade;
}