open module de.amr.games.pacman.uilib {
    // module is open to allow access to non-class resources

    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.media;
    requires org.tinylog.api;
    requires de.amr.games.pacman;
    requires java.sql;

    exports de.amr.pacmanfx.uilib;
    exports de.amr.pacmanfx.uilib.objimport;
    exports de.amr.pacmanfx.uilib.model3D;
    exports de.amr.pacmanfx.uilib.assets;
    exports de.amr.pacmanfx.uilib.widgets;
    exports de.amr.pacmanfx.uilib.animation;
    exports de.amr.pacmanfx.uilib.input;
    exports de.amr.pacmanfx.uilib.tilemap;
}