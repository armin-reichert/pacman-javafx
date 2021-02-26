module de.amr.games.pacman.ui.fx.jpms_pacman_app {

	requires javafx.base;
	requires transitive javafx.controls;
	requires javafx.graphics;
	requires de.amr.games.pacman.jpms_pacman_core;

	exports de.amr.games.pacman.ui.fx.app;
}