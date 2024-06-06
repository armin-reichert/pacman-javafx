/**
 * Module is open to give access to map files under resources folder.
 */
open module de.amr.games.pacman.mapeditor {

    requires javafx.graphics;
    requires javafx.controls;
    requires org.tinylog.api;
    requires de.amr.games.pacman.lib;

    exports de.amr.games.pacman.mapeditor;
}