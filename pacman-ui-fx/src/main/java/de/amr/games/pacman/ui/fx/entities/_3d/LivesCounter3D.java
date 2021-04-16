package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.function.Supplier;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.fx.model3D.GianmarcosModel3D;
import javafx.scene.Group;
import javafx.scene.Node;

public class LivesCounter3D implements Supplier<Node> {

	private final int numPositions = 5;
	private final Group root;

	public LivesCounter3D(Pac pac, int x, int y) {
		root = new Group();
		for (int i = 0; i < numPositions; ++i) {
			V2i tile = new V2i(x + 2 * i, y);
			Group liveIndicator = GianmarcosModel3D.IT.createPacMan();
			liveIndicator.setTranslateX(tile.x * TS);
			liveIndicator.setTranslateY(tile.y * TS);
			liveIndicator.setTranslateZ(0);
			root.getChildren().add(liveIndicator);
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