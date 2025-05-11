open module de.amr.games.pacman.tengen.ms_pacman {
    requires de.amr.games.pacman;

    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;

    requires org.tinylog.api;
    requires de.amr.games.pacman.uilib;
    requires de.amr.games.pacman.ui;
    exports de.amr.pacmanfx.tengen.ms_pacman;
}