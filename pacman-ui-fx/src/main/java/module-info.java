module de.amr.games.pacman.ui.fx {

	exports de.amr.games.pacman.ui.fx.app to javafx.graphics;

	requires transitive javafx.controls;
	requires javafx.media;
	requires jimObjModelImporterJFX;
	requires de.amr.games.pacman;
	requires javafx.graphics;
}