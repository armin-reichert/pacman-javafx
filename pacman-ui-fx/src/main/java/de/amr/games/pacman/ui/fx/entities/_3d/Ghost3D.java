package de.amr.games.pacman.ui.fx.entities._3d;

import java.util.function.Supplier;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import de.amr.games.pacman.ui.fx.rendering.GameRendering3D_Assets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;

/**
 * 3D ghost shape.
 * 
 * @author Armin Reichert
 */
public class Ghost3D implements Supplier<Node> {

	private final Ghost ghost;
	private final Group root;
	private final BountyShape3D bountyShape;
	private final PhongMaterial normalSkin;
	private final PhongMaterial blueSkin;
	private final MeshView meshView;

	@Override
	public Node get() {
		return root;
	}

	public Ghost3D(Ghost ghost, GameRendering2D rendering2D) {
		this.ghost = ghost;

		normalSkin = GameRendering3D_Assets.ghostSkin(ghost.id);
		blueSkin = new PhongMaterial(Color.CORNFLOWERBLUE);

		meshView = GameRendering3D_Assets.createGhostMeshView(ghost.id, 8);
		meshView.setMaterial(normalSkin);
		meshView.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		Group coloredGhost = new Group(meshView);

		bountyShape = new BountyShape3D(rendering2D);

		DeadGhost3D deadGhost = new DeadGhost3D(ghost);

		root = new Group(coloredGhost, bountyShape.get(), deadGhost.get());
		selectChild(0);
	}

	private void selectChild(int index) {
		for (int i = 0; i < 3; ++i) {
			root.getChildren().get(i).setVisible(i == index);
		}
	}

	public void update() {
		root.setVisible(ghost.visible);
		root.setTranslateX(ghost.position.x);
		root.setTranslateY(ghost.position.y);
		if (ghost.bounty > 0) {
			bountyShape.setBounty(ghost.bounty);
			root.setRotate(0);
			selectChild(1);
		} else if (ghost.is(GhostState.DEAD) || ghost.is(GhostState.ENTERING_HOUSE)) {
			root.setRotationAxis(Rotate.Z_AXIS);
			root.setRotate(ghost.dir == Direction.UP || ghost.dir == Direction.DOWN ? 90 : 0);
			selectChild(2);
		} else {
			meshView.setMaterial(ghost.is(GhostState.FRIGHTENED) ? blueSkin : normalSkin);
			turnTowardsMoveDirection();
			selectChild(0);
		}
	}

	private void turnTowardsMoveDirection() {
		root.setRotationAxis(Rotate.Y_AXIS);
		root.setRotate(0);
		root.setRotationAxis(Rotate.Z_AXIS);
		root.setRotate(
				ghost.dir == Direction.LEFT ? 180 : ghost.dir == Direction.RIGHT ? 0 : ghost.dir == Direction.UP ? -90 : 90);
	}
}