package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import javafx.scene.Group;
import javafx.scene.Node;

/**
 * Displays a Pac-Man for each live remaining.
 * 
 * @author Armin Reichert
 */
public class LivesCounter3D extends Group {

	static final int max = 5;

	public LivesCounter3D(PacManModel3D model3D) {
		for (int i = 0; i < max; ++i) {
			Node indicator = model3D.createPacMan();
			indicator.setTranslateX(2 * i * TS);
			getChildren().add(indicator);
		}
		setVisibleItems(max);
	}

	public void setVisibleItems(int n) {
		for (int i = 0; i < max; ++i) {
			getChildren().get(i).setVisible(i < n);
		}
	}
}