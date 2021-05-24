package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.fx.model3D.PacManModel3D;
import javafx.scene.Group;

/**
 * Displays a Pac-Man for each live remaining.
 * 
 * @author Armin Reichert
 */
public class LivesCounter3D extends Group {

	private final PacManModel3D model3D;
	private V2i tilePosition;
	private int maxEntries;
	private int livesCount;

	public LivesCounter3D(PacManModel3D model3D) {
		this.model3D = model3D;
		setTilePosition(V2i.NULL);
		setMaxEntries(5);
		setLivesCount(3);
	}

	public void setTilePosition(V2i tilePosition) {
		this.tilePosition = tilePosition;
	}

	public void setMaxEntries(int maxEntries) {
		if (this.maxEntries != maxEntries) {
			this.maxEntries = maxEntries;
			getChildren().clear();
			for (int i = 0; i < maxEntries; ++i) {
				V2i tile = tilePosition.plus(2 * i, 0);
				Group liveIndicator = model3D.createPacMan();
				liveIndicator.setTranslateX(tile.x * TS);
				liveIndicator.setTranslateY(tile.y * TS);
				getChildren().add(liveIndicator);
			}
		}
	}

	public int getLivesCount() {
		return livesCount;
	}

	public void setLivesCount(int livesCount) {
		this.livesCount = livesCount;
		for (int i = 0; i < maxEntries; ++i) {
			getChildren().get(i).setVisible(i < livesCount);
		}
	}
}