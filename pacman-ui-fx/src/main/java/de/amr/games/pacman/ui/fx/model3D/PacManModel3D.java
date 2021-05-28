package de.amr.games.pacman.ui.fx.model3D;

import javafx.scene.Group;

/**
 * Pac-Man 3D model interface.
 * 
 * @author Armin Reichert
 */
public interface PacManModel3D {

	Group createPacMan();

	Group createGhost();

	Group createGhostEyes();

}