module de.amr.games.pacman.ui3d.all {
    requires org.tinylog.api;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;

    requires de.amr.games.pacman;
    requires de.amr.games.pacman.uilib;
    requires de.amr.games.pacman.ui;
    requires de.amr.games.pacman.arcade.ms_pacman;
    requires de.amr.games.pacman.arcade.pacman;
    requires de.amr.games.pacman.arcade.pacman_xxl;
    requires de.amr.games.pacman.tengen.ms_pacman;

    exports de.amr.pacmanfx.allgames;
}