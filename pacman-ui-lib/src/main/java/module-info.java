open module de.amr.games.pacman.uilib {
    // module is open to allow access to non-class resources

    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.media;
    requires org.tinylog.api;
    requires de.amr.games.pacman;
    requires java.sql;

    exports de.amr.games.pacman.uilib;
    exports de.amr.games.pacman.uilib.objimport;
    exports de.amr.games.pacman.uilib.model3D;
    exports de.amr.games.pacman.uilib.assets;
    exports de.amr.games.pacman.uilib.widgets;
    exports de.amr.games.pacman.uilib.animation;
    exports de.amr.games.pacman.uilib.input;
}