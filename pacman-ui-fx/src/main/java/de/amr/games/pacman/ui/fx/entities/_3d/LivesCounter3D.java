package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.PacManGameModel;
import de.amr.games.pacman.ui.fx.model3D.GianmarcosModel3D;
import javafx.scene.Group;

/**
 * Displays a Pac-Man for each live remaining.
 * 
 * @author Armin Reichert
 */
public class LivesCounter3D extends Group {

	private V2i tilePosition;
	private int maxEntries;

	public LivesCounter3D(V2i tilePosition) {
		this.tilePosition = tilePosition;
		setMaxEntries(5);
	}

	public void setMaxEntries(int maxEntries) {
		if (this.maxEntries != maxEntries) {
			this.maxEntries = maxEntries;
			getChildren().clear();
			for (int i = 0; i < maxEntries; ++i) {
				V2i tile = tilePosition.plus(2 * i, 0);
				Group liveIndicator = GianmarcosModel3D.createPacMan();
				liveIndicator.setTranslateX(tile.x * TS);
				liveIndicator.setTranslateY(tile.y * TS);
				getChildren().add(liveIndicator);
			}
		}
	}

	public void update(PacManGameModel game) {
		for (int i = 0; i < maxEntries; ++i) {
			getChildren().get(i).setVisible(i < game.lives());
		}
	}
}