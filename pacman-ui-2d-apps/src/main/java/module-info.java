module de.amr.games.pacman.ui2d.apps {
    requires org.tinylog.api;
    requires javafx.graphics;
    requires de.amr.games.pacman;
    requires de.amr.games.pacman.lib;
    requires de.amr.games.pacman.ui2d;
    requires de.amr.games.pacman.arcade.ms_pacman;
    requires de.amr.games.pacman.arcade.pacman;
    requires de.amr.games.pacman.arcade.pacman_xxl;
    requires de.amr.games.pacman.tengen.ms_pacman;

    exports de.amr.games.pacman.ui2d.apps;
}