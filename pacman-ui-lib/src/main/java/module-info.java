open module de.amr.games.pacman.uilib {
    // module is open to allow access to non-class resources

    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.media;
    requires org.tinylog.api;
    requires de.amr.games.pacman;

    exports de.amr.games.pacman.uilib;

}