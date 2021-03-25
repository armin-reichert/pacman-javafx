module de.amr.games.pacman.ui.fx {

	requires de.amr.games.pacman;
	requires jimObjModelImporterJFX;
	requires java.desktop;
	requires transitive javafx.controls;

	exports de.amr.games.pacman.ui.fx.app to javafx.graphics;
}