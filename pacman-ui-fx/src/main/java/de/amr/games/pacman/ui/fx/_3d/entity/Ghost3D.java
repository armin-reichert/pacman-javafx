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

import static de.amr.games.pacman.model.world.World.HTS;

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
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 3D-representation of a ghost. A ghost is displayed in one of 3 modes: as a full ghost, as eyes only or as a bonus
 * symbol indicating the bounty paid for killing the ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost3D extends Creature3D {

	private enum DisplayMode {
		FULL, EYES, BOUNTY
	}

	private static class FlashingAnimation extends Transition {

		private PhongMaterial material = new PhongMaterial();

		public FlashingAnimation() {
			setCycleCount(INDEFINITE);
			setCycleDuration(Duration.seconds(0.1));
			setAutoReverse(true);
		}

		@Override
		protected void interpolate(double t) {
			material.setDiffuseColor(Color.rgb((int) (t * 120), (int) (t * 180), 255));
		}
	};

	public final Ghost ghost;

	private final Rendering2D rendering2D;
	private final Group completeGhost;
	private final Shape3D skin;
	private final Group eyes;
	private final Box numberCube;
	private final RotateTransition turningAnimation;
	private final FlashingAnimation flashing = new FlashingAnimation();

	private DisplayMode displayMode;
	private Direction targetDir;

	public Ghost3D(Ghost ghost, Group ghostComplete3D, Group ghostEyes3D, Rendering2D rendering2D) {
		this.ghost = ghost;
		this.targetDir = ghost.dir();
		this.rendering2D = rendering2D;

		completeGhost = ghostComplete3D;
		skin = (Shape3D) completeGhost.getChildren().get(0); // TODO cleanup
		eyes = ghostEyes3D;

		Group ghostVariants = new Group(completeGhost, eyes);
		ghostVariants.setRotationAxis(Rotate.Z_AXIS);
		ghostVariants.setRotate(rotationAngle(ghost.dir()));
		turningAnimation = new RotateTransition(Duration.seconds(0.3), ghostVariants);
		turningAnimation.setAxis(Rotate.Z_AXIS);

		numberCube = new Box(8, 8, 8);

		getChildren().addAll(ghostVariants, numberCube);

		displayCompleteGhost();
		setNormalSkinColor();
		setTranslateZ(-4);
	}

	private void displayNumberCube() {
		if (displayMode == DisplayMode.BOUNTY) {
			return;
		}
		Rectangle2D sprite = rendering2D.getBountyNumberSprites().get(ghost.bounty);
		Image image = rendering2D.extractRegion(sprite);
		PhongMaterial material = new PhongMaterial();
		material.setBumpMap(image);
		material.setDiffuseMap(image);
		numberCube.setMaterial(material);
		numberCube.setVisible(true);
		completeGhost.setVisible(false);
		eyes.setVisible(false);
		setRotationAxis(Rotate.X_AXIS);
		setRotate(0);
		displayMode = DisplayMode.BOUNTY;
	}

	private void displayEyes() {
		if (displayMode != DisplayMode.EYES) {
			completeGhost.setVisible(false);
			eyes.setVisible(true);
			numberCube.setVisible(false);
			displayMode = DisplayMode.EYES;
		}
		updateDirection();
	}

	private void displayCompleteGhost() {
		if (displayMode != DisplayMode.FULL) {
			completeGhost.setVisible(true);
			eyes.setVisible(false);
			numberCube.setVisible(false);
			displayMode = DisplayMode.FULL;
		}
		updateDirection();
	}

	public void update() {
		if (ghost.bounty > 0) {
			displayNumberCube();
		} else if (ghost.is(GhostState.DEAD) || ghost.is(GhostState.ENTERING_HOUSE)) {
			displayEyes();
		} else {
			displayCompleteGhost();
		}
		setTranslateX(ghost.position.x + HTS);
		setTranslateY(ghost.position.y + HTS);
		setVisible(ghost.visible && !outsideMaze(ghost));
	}

	private void updateDirection() {
		if (targetDir != ghost.dir()) {
			int[] angles = rotationAngles(targetDir, ghost.dir());
			turningAnimation.setFromAngle(angles[0]);
			turningAnimation.setToAngle(angles[1]);
			turningAnimation.playFromStart();
			targetDir = ghost.dir();
		}
	}

	public void playFlashingAnimation() {
		skin.setMaterial(flashing.material);
		flashing.playFromStart();
	}

	public void setNormalSkinColor() {
		flashing.stop();
		setSkinColor(rendering2D.getGhostColor(ghost.id));
	}

	public void setBlueSkinColor() {
		flashing.stop();
		setSkinColor(Color.CORNFLOWERBLUE);
	}

	private void setSkinColor(Color color) {
		PhongMaterial material = new PhongMaterial(color);
		material.setSpecularColor(color.brighter());
		skin.setMaterial(material);
	}
}