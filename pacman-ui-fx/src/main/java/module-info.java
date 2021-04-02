module de.amr.games.pacman.ui.fx {

	requires de.amr.games.pacman;
	requires jimObjModelImporterJFX;
	requires transitive javafx.controls;
	requires transitive javafx.media;
	requires javafx.graphics;

	exports de.amr.games.pacman.ui.fx.app to javafx.graphics;
}