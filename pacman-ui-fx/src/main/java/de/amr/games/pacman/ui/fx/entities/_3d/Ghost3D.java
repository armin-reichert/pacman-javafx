package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.ui.fx.rendering.GameRendering3D_Assets.createGhostMeshView;
import static de.amr.games.pacman.ui.fx.rendering.GameRendering3D_Assets.getGhostColor;

import java.util.function.Supplier;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
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
	private final Group root = new Group();
	private final MeshView coloredGhost;
	private final DeadGhost3D deadGhost;
	private final BountyShape3D bountyShape;
	private final PhongMaterial normalSkin;
	private final PhongMaterial blueSkin;

	public Ghost3D(Ghost ghost, GameRendering2D rendering2D) {
		this.ghost = ghost;
		normalSkin = new PhongMaterial(getGhostColor(ghost.id));
		blueSkin = new PhongMaterial(Color.CORNFLOWERBLUE);
		coloredGhost = createGhostMeshView(ghost.id, 8);
		coloredGhost.setMaterial(normalSkin);
		coloredGhost.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		bountyShape = new BountyShape3D(rendering2D);
		deadGhost = new DeadGhost3D(ghost);
		setCurrentNode(coloredGhost);
	}

	@Override
	public Node get() {
		return root;
	}

	private void setCurrentNode(Node node) {
		root.getChildren().clear();
		root.getChildren().add(node);
	}

	public void update() {
		root.setVisible(ghost.visible);
		root.setTranslateX(ghost.position.x);
		root.setTranslateY(ghost.position.y);
		if (ghost.bounty > 0) {
			setCurrentNode(bountyShape.get());
			bountyShape.setBounty(ghost.bounty);
			root.setRotate(0);
		} else if (ghost.is(GhostState.DEAD) || ghost.is(GhostState.ENTERING_HOUSE)) {
			setCurrentNode(deadGhost.get());
			root.setRotationAxis(Rotate.Z_AXIS);
			root.setRotate(ghost.dir == Direction.UP || ghost.dir == Direction.DOWN ? 90 : 0);
		} else {
			setCurrentNode(coloredGhost);
			coloredGhost.setMaterial(ghost.is(GhostState.FRIGHTENED) ? blueSkin : normalSkin);
			root.setRotationAxis(Rotate.Y_AXIS);
			root.setRotate(0);
			root.setRotationAxis(Rotate.Z_AXIS);
			root.setRotate(
					ghost.dir == Direction.LEFT ? 180 : ghost.dir == Direction.RIGHT ? 0 : ghost.dir == Direction.UP ? -90 : 90);
		}
	}
}