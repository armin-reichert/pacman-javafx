package de.amr.games.pacman.ui.fx.common.scene3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.List;
import java.util.Optional;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Pac;
import javafx.scene.Group;
import javafx.scene.Node;

public class LivesCounter3D {

	private final int numPositions = 5;
	private final V2i origin;
	private final Group root;
	private final List<Brick3D> bricks;

	public LivesCounter3D(Pac pac, List<Brick3D> bricks, V2i origin) {
		this.bricks = bricks;
		this.origin = origin;
		root = new Group();
		root.setViewOrder(-origin.y * TS);
		for (int i = 0; i < numPositions; ++i) {
			V2i brickTile = new V2i(origin.x + 2 * i, origin.y);
			Player3D liveIndicator = new Player3D(pac);
			liveIndicator.getNode().setTranslateX(brickTile.x * TS);
			liveIndicator.getNode().setTranslateY(brickTile.y * TS);
			liveIndicator.getNode().setTranslateZ(4); // ???
			root.getChildren().add(liveIndicator.getNode());
		}
	}

	public void update(GameModel game) {
		for (int i = 0; i < numPositions; ++i) {
			V2i indicatorTile = new V2i(origin.x + 2 * i, origin.y);
			V2i tileBelow = indicatorTile.plus(0, 1);
			Optional<Node> nodeAtIndicatorPosition = bricks.stream()
					.filter(brick3D -> indicatorTile.equals(brick3D.getTile())).map(Brick3D::getNode).findFirst();
			Optional<Node> nodeBelow = bricks.stream().filter(brick3D -> tileBelow.equals(brick3D.getTile()))
					.map(Brick3D::getNode).findFirst();
			boolean visible = i < game.lives;
			root.getChildren().get(i).setVisible(visible);
			nodeAtIndicatorPosition.ifPresent(node -> node.setVisible(!visible));
			nodeBelow.ifPresent(node -> node.setVisible(!visible));
		}
	}

	public Node getNode() {
		return root;
	}
}