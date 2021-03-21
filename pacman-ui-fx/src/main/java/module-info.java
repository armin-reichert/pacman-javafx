module de.amr.games.pacman.ui.fx {

	// TODO implement sound with FX code
	requires transitive java.desktop;
	requires transitive javafx.controls;
	requires de.amr.games.pacman;

	// this should come from a public repository
	requires jimModelImporterJFX;

	exports de.amr.games.pacman.ui.fx.app to javafx.graphics;
}