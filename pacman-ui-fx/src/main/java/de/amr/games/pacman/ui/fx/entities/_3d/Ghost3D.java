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
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;

/**
 * 3D ghost shape.
 * 
 * @author Armin Reichert
 */
public class Ghost3D extends Group implements Supplier<Node> {

	private final Ghost ghost;
	private final Rendering2D rendering2D;
	private final Group coloredGhost;
	private PhongMaterial normalSkin;
	private PhongMaterial blueSkin;
	private final Group deadGhost;
	private final Box bountyShape;

	public Ghost3D(Ghost ghost, Rendering2D rendering2D) {
		this.ghost = ghost;
		this.rendering2D = rendering2D;
		normalSkin = new PhongMaterial(Rendering2D_Assets.getGhostColor(ghost.id));
		blueSkin = new PhongMaterial(Color.CORNFLOWERBLUE);
		coloredGhost = GianmarcosModel3D.IT.createGhost();
		bountyShape = new Box(8, 8, 8);
		bountyShape.setMaterial(new PhongMaterial());
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

	private void setBounty(int value) {
		Image sprite = rendering2D.subImage(rendering2D.getBountyNumberSpritesMap().get(value));
		PhongMaterial material = (PhongMaterial) bountyShape.getMaterial();
		material.setBumpMap(sprite);
		material.setDiffuseMap(sprite);
	}

	public void update() {
		setVisible(ghost.visible);
		setTranslateX(ghost.position.x);
		setTranslateY(ghost.position.y);
		if (ghost.bounty > 0) {
			getChildren().setAll(bountyShape);
			setRotationAxis(Rotate.X_AXIS);
			setRotate(0);
			setBounty(ghost.bounty);
		} else if (ghost.is(GhostState.DEAD) || ghost.is(GhostState.ENTERING_HOUSE)) {
			getChildren().setAll(deadGhost);
			rotateZ(ghost.dir, 0, 180, 90, -90);
		} else {
			getChildren().setAll(coloredGhost);
			setRotationAxis(Rotate.Y_AXIS);
			setRotate(0);
			rotateZ(ghost.is(GhostState.FRIGHTENED) ? ghost.dir : ghost.wishDir, 0, 180, 90, -90);
			setBlueSkin(ghost.is(GhostState.FRIGHTENED));
		}
	}

	private void rotateZ(Direction dir, int left, int right, int up, int down) {
		setRotationAxis(Rotate.Z_AXIS);
		setRotate(dir == Direction.LEFT ? left : dir == Direction.RIGHT ? right : dir == Direction.UP ? up : down);
	}
}