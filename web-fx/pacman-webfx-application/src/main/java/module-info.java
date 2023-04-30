// File managed by WebFX (DO NOT EDIT MANUALLY)

module pacman.webfx.application {

	// Direct dependencies modules
	requires javafx.graphics;

	requires transitive de.amr.games.pacman;
	requires transitive de.amr.games.pacman.ui.fx;
	requires transitive org.tinylog.api;

	// Exported packages
	exports de.amr.games.pacman.webfx;

	// Provided services
	provides javafx.application.Application with de.amr.games.pacman.webfx.PacManWebFXApp;

}