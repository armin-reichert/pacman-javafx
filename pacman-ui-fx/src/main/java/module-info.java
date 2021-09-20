module de.amr.games.pacman.ui.fx {

	exports de.amr.games.pacman.ui.fx.app to javafx.graphics;
	
	// for Fxyz importer test
	exports de.amr.games.pacman.ui.fx._3d.entity;
	exports de.amr.games.pacman.ui.fx._2d.rendering.common;

	requires transitive javafx.controls;
	requires transitive javafx.media;

	requires transitive de.amr.games.pacman;
	requires jimObjModelImporterJFX; // TODO use "real" module
}