module org.poloskai {

	exports org.poloskai to javafx.graphics;

	requires transitive de.amr.games.pacman;
	requires transitive de.amr.games.pacman.ui.fx;
	requires transitive javafx.controls;
	requires transitive javafx.media;

}