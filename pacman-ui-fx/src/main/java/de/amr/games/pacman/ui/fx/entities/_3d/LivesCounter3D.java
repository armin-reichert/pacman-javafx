package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.function.Supplier;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.common.Pac;
import javafx.scene.Group;
import javafx.scene.Node;

public class LivesCounter3D implements Supplier<Node> {

	private final int numPositions = 5;
	private final Group root;

	public LivesCounter3D(Pac pac, int x, int y) {
		root = new Group();
		root.setViewOrder(-y * TS - TS);
		for (int i = 0; i < numPositions; ++i) {
			V2i brickTile = new V2i(x + 2 * i, y);
			Player3D liveIndicator = new Player3D(pac);
			liveIndicator.get().setTranslateX(brickTile.x * TS);
			liveIndicator.get().setTranslateY(brickTile.y * TS);
			liveIndicator.get().setTranslateZ(-TS); // ???
			root.getChildren().add(liveIndicator.get());
		}
	}

	public void update(AbstractGameModel game) {
		for (int i = 0; i < numPositions; ++i) {
			root.getChildren().get(i).setVisible(i < game.lives);
		}
	}

	@Override
	public Node get() {
		return root;
	}
}