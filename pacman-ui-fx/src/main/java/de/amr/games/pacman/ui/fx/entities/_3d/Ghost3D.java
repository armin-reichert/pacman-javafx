package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.fx.model3D.PacManModel3D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_Assets;
import javafx.animation.RotateTransition;
import javafx.animation.Transition;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 3D-representation of a ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost3D extends Group {

	//@formatter:off
	private static final int[][][] ROTATION_INTERVALS = {
		{ {  0, 0}, {  0, 180}, {  0, 90}, {  0, -90} },
		{ {180, 0}, {180, 180}, {180, 90}, {180, 270} }, 
		{ { 90, 0}, { 90, 180}, { 90, 90}, { 90, 270} },
		{ {-90, 0}, {270, 180}, {-90, 90}, {-90, -90} },
	};
	//@formatter:on

	private static int index(Direction dir) {
		return dir == Direction.LEFT ? 0 : dir == Direction.RIGHT ? 1 : dir == Direction.UP ? 2 : 3;
	}

	private static int[] rotationInterval(Direction from, Direction to) {
		return ROTATION_INTERVALS[index(from)][index(to)];
	}

	private static Duration TURNING_DURATION = Duration.seconds(0.25);

	public class FlashingAnimation extends Transition {

		public FlashingAnimation() {
			setCycleCount(INDEFINITE);
			setCycleDuration(Duration.seconds(0.1));
			setAutoReverse(true);
		}

		@Override
		protected void interpolate(double frac) {
			skinMaterial.setDiffuseColor(Color.rgb((int) (frac * 120), (int) (frac * 180), 255));
		}

		@Override
		public void stop() {
			super.stop();
			setNormalSkinColor();
		}
	};

	public final Ghost ghost;
	public final FlashingAnimation flashingAnimation = new FlashingAnimation();
	private final Color normalColor;
	private final Rendering2D rendering2D;
	private final Group ghostShape;
	private final MeshView body;
	private final RotateTransition ghostShapeRot;
	private final Group eyesShape;
	private final RotateTransition eyesShapeRot;
	private final Box bountyShape;
	private final PhongMaterial skinMaterial = new PhongMaterial();
	private Direction targetDir;

	public Ghost3D(Ghost ghost, PacManModel3D model3D, Rendering2D rendering2D) {
		this.ghost = ghost;
		this.targetDir = ghost.dir();
		this.rendering2D = rendering2D;
		this.normalColor = Rendering2D_Assets.getGhostColor(ghost.id);

		int[] rotationInterval = rotationInterval(ghost.dir(), targetDir);

		ghostShape = model3D.createGhost();
		ghostShape.setRotationAxis(Rotate.Z_AXIS);
		ghostShape.setRotate(rotationInterval[0]);

		body = (MeshView) ghostShape.getChildren().get(0);
		body.setMaterial(skinMaterial);

		ghostShapeRot = new RotateTransition(TURNING_DURATION, ghostShape);
		ghostShapeRot.setAxis(Rotate.Z_AXIS);

		eyesShape = model3D.createGhostEyes();
		eyesShape.setRotationAxis(Rotate.Z_AXIS);
		eyesShape.setRotate(rotationInterval[0]);

		eyesShapeRot = new RotateTransition(TURNING_DURATION, eyesShape);
		eyesShapeRot.setAxis(Rotate.Z_AXIS);

		bountyShape = new Box(8, 8, 8);
		bountyShape.setMaterial(new PhongMaterial());

		setNormalSkinColor();
		getChildren().setAll(ghostShape);
		setTranslateZ(-4);
	}

	public void update() {
		setVisible(ghost.isVisible());
		setTranslateX(ghost.position().x);
		setTranslateY(ghost.position().y);
		if (ghost.bounty > 0) {
			if (getChildren().get(0) != bountyShape) {
				Rectangle2D sprite = rendering2D.getBountyNumberSprites().get(ghost.bounty);
				Image image = rendering2D.sheet().subImage(sprite);
				PhongMaterial material = (PhongMaterial) bountyShape.getMaterial();
				material.setBumpMap(image);
				material.setDiffuseMap(image);
				getChildren().setAll(bountyShape);
				log("Set bounty mode for %s", ghost);
			}
			setRotationAxis(Rotate.X_AXIS);
			setRotate(0);
		} else if (ghost.is(GhostState.DEAD) || ghost.is(GhostState.ENTERING_HOUSE)) {
			getChildren().setAll(eyesShape);
			rotateTowardsMoveDir();
		} else {
			getChildren().setAll(ghostShape);
			rotateTowardsMoveDir();
		}
	}

	public void setNormalSkinColor() {
		setSkinColor(normalColor);
	}

	public void setBlueSkinColor() {
		flashingAnimation.stop(); // iny case it was playing
		setSkinColor(Color.CORNFLOWERBLUE);
	}

	private void setSkinColor(Color skinColor) {
		skinMaterial.setDiffuseColor(skinColor);
		skinMaterial.setSpecularColor(skinColor.brighter());
		body.setMaterial(skinMaterial);
	}

	private void rotateTowardsMoveDir() {
		if (targetDir != ghost.dir()) {
			int[] rotationInterval = rotationInterval(targetDir, ghost.dir());
			ghostShapeRot.stop();
			ghostShapeRot.setFromAngle(rotationInterval[0]);
			ghostShapeRot.setToAngle(rotationInterval[1]);
			ghostShapeRot.play();
			eyesShapeRot.stop();
			eyesShapeRot.setFromAngle(rotationInterval[0]);
			eyesShapeRot.setToAngle(rotationInterval[1]);
			eyesShapeRot.play();
			targetDir = ghost.dir();
		}
	}
}