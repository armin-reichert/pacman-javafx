/**
 * Module is open to give access to map files under resources folder.
 */
open module de.amr.games.pacman.tilemap {

    requires javafx.graphics;
    requires javafx.controls;
    requires org.tinylog.api;
    requires de.amr.games.pacman;

    exports de.amr.games.pacman.tilemap.editor;
    exports de.amr.games.pacman.tilemap.rendering;
}