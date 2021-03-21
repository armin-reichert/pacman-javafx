package de.amr.games.pacman.ui.fx.common.scene3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Pac;
import javafx.scene.Group;
import javafx.scene.Node;

public class LivesCounter3D {

	private final int numPositions = 5;
	private final Group root;

	public LivesCounter3D(Pac pac, int x, int y) {
		root = new Group();
		root.setViewOrder(-y * TS - TS);
		for (int i = 0; i < numPositions; ++i) {
			V2i brickTile = new V2i(x + 2 * i, y);
			Player3D liveIndicator = new Player3D(pac);
			liveIndicator.getNode().setTranslateX(brickTile.x * TS);
			liveIndicator.getNode().setTranslateY(brickTile.y * TS);
			liveIndicator.getNode().setTranslateZ(-TS); // ???
			root.getChildren().add(liveIndicator.getNode());
		}
	}

	public void update(GameModel game) {
		for (int i = 0; i < numPositions; ++i) {
			root.getChildren().get(i).setVisible(i < game.lives);
		}
	}

	public Node getNode() {
		return root;
	}
}