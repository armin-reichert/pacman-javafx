module de.amr.games.pacman.ui.fx {

	// TODO implement sound with FX code
	requires transitive java.desktop;

	requires transitive javafx.controls;
	requires transitive javafx.graphics;

	requires de.amr.games.pacman;

	exports de.amr.games.pacman.ui.fx.app to javafx.graphics;
}