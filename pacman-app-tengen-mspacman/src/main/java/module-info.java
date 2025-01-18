open module de.amr.games.pacman.tengen.ms_pacman {
    requires de.amr.games.pacman;

    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;

    requires org.tinylog.api;
    requires de.amr.games.pacman.mapeditor;
    requires de.amr.games.pacman.ui3d;

    exports de.amr.games.pacman.tengen.ms_pacman;
    exports de.amr.games.pacman.tengen.ms_pacman.maps;
    exports de.amr.games.pacman.tengen.ms_pacman.rendering2d;
    exports de.amr.games.pacman.tengen.ms_pacman.scene;
}