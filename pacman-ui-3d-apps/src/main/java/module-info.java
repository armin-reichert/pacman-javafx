module de.amr.games.pacman.ui3d.apps {
    requires org.tinylog.api;
    requires javafx.graphics;
    requires javafx.media;

    requires de.amr.games.pacman;
    requires de.amr.games.pacman.lib;
    requires de.amr.games.pacman.ui3d;
    requires de.amr.games.pacman.ui2d;
    requires de.amr.games.pacman.arcade.ms_pacman;
    requires de.amr.games.pacman.arcade.pacman;
    requires de.amr.games.pacman.arcade.pacman_xxl;
    requires de.amr.games.pacman.tengen.ms_pacman;
    requires de.amr.games.pacman.ui;

    exports de.amr.games.pacman.ui3d.apps;
}