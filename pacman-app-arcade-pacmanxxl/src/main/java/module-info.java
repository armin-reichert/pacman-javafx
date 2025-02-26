open module de.amr.games.pacman.arcade.pacman_xxl {
    requires org.tinylog.api;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;

    requires de.amr.games.pacman;
    requires de.amr.games.pacman.tilemap;
    requires de.amr.games.pacman.arcade.ms_pacman;
    requires de.amr.games.pacman.arcade.pacman;
    requires de.amr.games.pacman.ui;
    requires de.amr.games.pacman.uilib;

    exports de.amr.games.pacman.arcade.pacman_xxl;
}