/*
MIT License

Copyright (c) 2021 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx._3d.entity;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
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
public class Ghost3D extends Creature3D {

	private static class FlashingAnimation extends Transition {

		private PhongMaterial material = new PhongMaterial();

		public FlashingAnimation() {
			setCycleCount(INDEFINITE);
			setCycleDuration(Duration.seconds(0.1));
			setAutoReverse(true);
		}

		@Override
		protected void interpolate(double frac) {
			material.setDiffuseColor(Color.rgb((int) (frac * 120), (int) (frac * 180), 255));
		}
	};

	public final Ghost ghost;
	private final FlashingAnimation flashing = new FlashingAnimation();
	private final Color normalColor;
	private final Rendering2D rendering2D;
	private final Group body;
	private final MeshView skin;
	private final RotateTransition bodyTurningAnimation;
	private final Group eyes;
	private final RotateTransition eyesTurningAnimation;
	private final Box bounty;
	private final PhongMaterial skinMaterial = new PhongMaterial();
	private Direction targetDir;

	public Ghost3D(Ghost ghost, PacManModel3D model3D, Rendering2D rendering2D) {
		this.ghost = ghost;
		this.targetDir = ghost.dir();
		this.rendering2D = rendering2D;
		this.normalColor = rendering2D.getGhostColor(ghost.id);

		int[] angles = rotationAngles(ghost.dir(), targetDir);

		body = model3D.createGhost();
		body.setRotationAxis(Rotate.Z_AXIS);
		body.setRotate(angles[0]);

		skin = (MeshView) body.getChildren().get(0);
		skin.setMaterial(skinMaterial);

		bodyTurningAnimation = new RotateTransition(Duration.seconds(0.25), body);
		bodyTurningAnimation.setAxis(Rotate.Z_AXIS);

		eyes = model3D.createGhostEyes();
		eyes.setRotationAxis(Rotate.Z_AXIS);
		eyes.setRotate(angles[0]);

		eyesTurningAnimation = new RotateTransition(Duration.seconds(0.25), eyes);
		eyesTurningAnimation.setAxis(Rotate.Z_AXIS);

		bounty = new Box(8, 8, 8);
		bounty.setMaterial(new PhongMaterial());

		getChildren().setAll(body);
		setNormalSkinColor();
		setTranslateZ(-4);
	}

	public void update() {
		setVisible(ghost.isVisible() && !outsideMaze(ghost));
		setTranslateX(ghost.position().x);
		setTranslateY(ghost.position().y);
		if (ghost.bounty > 0) {
			if (getChildren().get(0) != bounty) {
				Rectangle2D sprite = rendering2D.getBountyNumberSprites().get(ghost.bounty);
				Image image = rendering2D.createSubImage(sprite);
				PhongMaterial material = (PhongMaterial) bounty.getMaterial();
				material.setBumpMap(image);
				material.setDiffuseMap(image);
				getChildren().setAll(bounty);
			}
			setRotationAxis(Rotate.X_AXIS);
			setRotate(0);
		} else if (ghost.is(GhostState.DEAD) || ghost.is(GhostState.ENTERING_HOUSE)) {
			getChildren().setAll(eyes);
			turn();
		} else {
			getChildren().setAll(body);
			turn();
		}
	}

	private void turn() {
		if (targetDir != ghost.dir()) {
			int[] angles = rotationAngles(targetDir, ghost.dir());
			bodyTurningAnimation.stop();
			bodyTurningAnimation.setFromAngle(angles[0]);
			bodyTurningAnimation.setToAngle(angles[1]);
			bodyTurningAnimation.play();
			eyesTurningAnimation.stop();
			eyesTurningAnimation.setFromAngle(angles[0]);
			eyesTurningAnimation.setToAngle(angles[1]);
			eyesTurningAnimation.play();
			targetDir = ghost.dir();
		}
	}

	public void playFlashingAnimation() {
		skin.setMaterial(flashing.material);
		flashing.playFromStart();
	}

	public void stopFlashingAnimation() {
		flashing.stop();
		setNormalSkinColor();
	}

	public void setNormalSkinColor() {
		setSkinColor(normalColor);
	}

	public void setBlueSkinColor() {
		flashing.stop();
		setSkinColor(Color.CORNFLOWERBLUE);
	}

	private void setSkinColor(Color skinColor) {
		skinMaterial.setDiffuseColor(skinColor);
		skinMaterial.setSpecularColor(skinColor.brighter());
		skin.setMaterial(skinMaterial);
	}
}