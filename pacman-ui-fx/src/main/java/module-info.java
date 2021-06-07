module de.amr.games.pacman.ui.fx {

	exports de.amr.games.pacman.ui.fx.app to javafx.graphics;
	exports de.amr.games.pacman.ui.fx.model3D;
	
	requires transitive javafx.controls;
	requires transitive javafx.media;
	requires de.amr.games.pacman;
	requires jimObjModelImporterJFX;
}