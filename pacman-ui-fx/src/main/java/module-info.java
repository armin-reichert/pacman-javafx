module de.amr.games.pacman.ui.fx {

	// TODO implement sound with FX code
	requires transitive java.desktop;
	requires transitive javafx.controls;
	requires de.amr.games.pacman;

	requires jimObjModelImporterJFX;

	exports de.amr.games.pacman.ui.fx.app to javafx.graphics;
}