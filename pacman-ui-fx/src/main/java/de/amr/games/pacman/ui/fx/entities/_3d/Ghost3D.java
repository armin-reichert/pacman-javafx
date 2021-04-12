package de.amr.games.pacman.ui.fx.entities._3d;

import java.util.function.Supplier;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;

/**
 * 3D ghost shape.
 * 
 * @author Armin Reichert
 */
public class Ghost3D implements Supplier<Node> {

	private final Ghost ghost;
	private final Group root = new Group();
	private final ColoredGhost3D coloredGhost;
	private final DeadGhost3D deadGhost;
	private final BountyShape3D bountyShape;

	public Ghost3D(Ghost ghost, GameRendering2D rendering2D) {
		this.ghost = ghost;
		coloredGhost = new ColoredGhost3D(ghost);
		bountyShape = new BountyShape3D(rendering2D);
		deadGhost = new DeadGhost3D(ghost);
	}

	@Override
	public Node get() {
		return root;
	}

	private void select(Supplier<Node> node) {
		root.getChildren().clear();
		root.getChildren().add(node.get());
	}

	public void update() {
		root.setVisible(ghost.visible);
		root.setTranslateX(ghost.position.x);
		root.setTranslateY(ghost.position.y);
		if (ghost.bounty > 0) {
			root.setRotationAxis(Rotate.X_AXIS);
			root.setRotate(0);
			bountyShape.setBounty(ghost.bounty);
			select(bountyShape);
		} else if (ghost.is(GhostState.DEAD) || ghost.is(GhostState.ENTERING_HOUSE)) {
			root.setRotationAxis(Rotate.Z_AXIS);
			root.setRotate(ghost.dir == Direction.UP || ghost.dir == Direction.DOWN ? 90 : 0);
			select(deadGhost);
		} else {
			root.setRotationAxis(Rotate.Y_AXIS);
			root.setRotate(0);
			root.setRotationAxis(Rotate.Z_AXIS);
			root.setRotate(
					ghost.dir == Direction.LEFT ? 180 : ghost.dir == Direction.RIGHT ? 0 : ghost.dir == Direction.UP ? -90 : 90);
			coloredGhost.setBlue(ghost.is(GhostState.FRIGHTENED));
			select(coloredGhost);
		}
	}
}