open module de.amr.games.pacman.arcade.ms_pacman {
    requires de.amr.games.pacman;
    requires de.amr.games.pacman.ui2d;

    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;

    requires org.tinylog.api;
    requires de.amr.games.pacman.ui3d;

    exports de.amr.games.pacman.arcade.ms_pacman;
}