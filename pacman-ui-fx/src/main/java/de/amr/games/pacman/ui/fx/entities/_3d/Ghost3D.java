package de.amr.games.pacman.ui.fx.entities._3d;

import java.util.function.Supplier;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.fx.model3D.GianmarcosModel3D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_Assets;
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
public class Ghost3D extends Group implements Supplier<Node> {

	private final Ghost ghost;
	private final Group coloredGhost;
	private PhongMaterial normalSkin;
	private PhongMaterial blueSkin;
	private final Group deadGhost;
	private final BountyShape3D bountyShape;

	public Ghost3D(Ghost ghost, Rendering2D rendering2D) {
		this.ghost = ghost;
		normalSkin = new PhongMaterial(Rendering2D_Assets.getGhostColor(ghost.id));
		blueSkin = new PhongMaterial(Color.CORNFLOWERBLUE);
		coloredGhost = GianmarcosModel3D.IT.createGhost();
		bountyShape = new BountyShape3D(rendering2D);
		deadGhost = GianmarcosModel3D.IT.createGhostEyes();
		setBlueSkin(false);
	}

	@Override
	public Node get() {
		return this;
	}

	private void setBlueSkin(boolean blue) {
		MeshView meshView = (MeshView) coloredGhost.getChildren().get(0);
		meshView.setMaterial(blue ? blueSkin : normalSkin);
	}

	private void setSingleChild(Node node) {
		getChildren().clear();
		getChildren().add(node);
	}

	public void update() {
		setVisible(ghost.visible);
		setTranslateX(ghost.position.x);
		setTranslateY(ghost.position.y);
		if (ghost.bounty > 0) {
			setRotationAxis(Rotate.X_AXIS);
			setRotate(0);
			bountyShape.setBounty(ghost.bounty);
			setSingleChild(bountyShape);
		} else if (ghost.is(GhostState.DEAD) || ghost.is(GhostState.ENTERING_HOUSE)) {
			setRotationAxis(Rotate.Z_AXIS);
			Direction dir = ghost.dir;
			setRotate(dir == Direction.LEFT ? 0 : dir == Direction.RIGHT ? 180 : dir == Direction.UP ? 90 : -90);
			setSingleChild(deadGhost);
		} else {
			setRotationAxis(Rotate.Y_AXIS);
			setRotate(0);
			setRotationAxis(Rotate.Z_AXIS);
			Direction dir = ghost.is(GhostState.FRIGHTENED) ? ghost.dir : ghost.wishDir;
			setRotate(dir == Direction.LEFT ? 0 : dir == Direction.RIGHT ? 180 : dir == Direction.UP ? 90 : -90);
			setBlueSkin(ghost.is(GhostState.FRIGHTENED));
			setSingleChild(coloredGhost);
		}
	}
}