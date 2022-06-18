/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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

import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._3d.animation.ColorFlashingTransition;
import de.amr.games.pacman.ui.fx._3d.animation.FadeInTransition3D;
import de.amr.games.pacman.ui.fx._3d.animation.Rendering3D;
import de.amr.games.pacman.ui.fx._3d.model.PacManModel3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 3D representation of a ghost. A ghost is displayed in one of 3 modes:
 * <ul>
 * <li>as a full ghost with colored skin and eyes,
 * <li>as eyes only,
 * <li>as a number cube indicating the value of the dead ghost.
 * </ul>
 * 
 * @author Armin Reichert
 */
public class Ghost3D extends Group implements Rendering3D {

	public enum AnimationMode {
		COLORED, FRIGHTENED, FLASHING, EYES_ONLY, NUMBER_CUBE;
	}

	public class NumberCubeAnimation {

		private final Box numberCube;
		private final Image[] valueImages;

		public NumberCubeAnimation(Rendering2D r2D) {
			numberCube = new Box(8, 8, 8);
			valueImages = r2D.createGhostValueList().frames().map(r2D::getSpriteImage).toArray(Image[]::new);
		}

		public Node getRoot() {
			return numberCube;
		}

		public void setNumber(int number) {
			var texture = valueImages[number];
			var material = new PhongMaterial();
			material.setBumpMap(texture);
			material.setDiffuseMap(texture);
			numberCube.setMaterial(material);
		}
	}

	public class BodyAnimation {

		private Group root;
		private final Motion motion;
		private boolean frightened;
		private ColorFlashingTransition flashing;

		public BodyAnimation(PacManModel3D model3D) {
			root = model3D.createGhost(ghostify(getGhostSkinColor(ghost.id)), getGhostEyeBallColor(), getGhostPupilColor());
			motion = new Motion(Ghost3D.this);
		}

		public Shape3D skin() {
			return (Shape3D) root.getChildren().get(0);
		}

		/**
		 * @return group representing eyes = (pupils, eyeBalls)
		 */
		public Group eyes() {
			return (Group) root.getChildren().get(1);
		}

		public Shape3D pupils() {
			return (Shape3D) eyes().getChildren().get(0);
		}

		public Shape3D eyeBalls() {
			return (Shape3D) eyes().getChildren().get(1);
		}

		public Node getRoot() {
			return root;
		}

		public void move() {
			motion.update(ghost);
			boolean insideWorld = 0 <= ghost.position.x && ghost.position.x <= t(ArcadeWorld.TILES_X - 1);
			root.setVisible(ghost.visible && insideWorld);
		}

		public void setShowSkin(boolean showSkin) {
			bodyAnimation.skin().setVisible(showSkin);
		}

		public void playFlashingAnimation() {
			flashing = new ColorFlashingTransition(getGhostSkinColorFrightened(), getGhostSkinColorFrightened2());
			skin().setMaterial(flashing.getMaterial());
			flashing.playFromStart();
		}

		public void stopFlashingAnimation() {
			if (flashing != null) {
				flashing.stop();
			}
		}

		public void playRevivalAnimation() {
			var animation = new FadeInTransition3D(Duration.seconds(1.5), skin(), ghostify(getGhostSkinColor(ghost.id)));
			animation.setOnFinished(e -> setAnimationMode(AnimationMode.COLORED));
			animation.playFromStart();
		}

		public void setFrightened(boolean frightened) {
			stopFlashingAnimation();
			this.frightened = frightened;
			if (frightened) {
				setShapeColor(skin(), ghostify(getGhostSkinColorFrightened()));
				setShapeColor(eyeBalls(), getGhostEyeBallColorFrightened());
				setShapeColor(pupils(), getGhostPupilColorFrightened());
			} else {
				setShapeColor(skin(), ghostify(getGhostSkinColor(ghost.id)));
				setShapeColor(eyeBalls(), getGhostEyeBallColor());
				setShapeColor(pupils(), getGhostPupilColor());

			}
		}

		private void setShapeColor(Shape3D shape, Color diffuseColor) {
			var material = new PhongMaterial(diffuseColor);
			material.setSpecularColor(diffuseColor.brighter());
			shape.setMaterial(material);
		}

		private Color ghostify(Color color) {
			return Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.90);
		}

	}

	public final Ghost ghost;
	private final NumberCubeAnimation numberCubeAnimation;
	private final BodyAnimation bodyAnimation;
	private AnimationMode animationMode;

	public Ghost3D(Ghost ghost, PacManModel3D model3D, Rendering2D r2D) {
		this.ghost = ghost;
		numberCubeAnimation = new NumberCubeAnimation(r2D);
		bodyAnimation = new BodyAnimation(model3D);
		reset();
	}

	public void reset() {
		bodyAnimation.setFrightened(false);
		update();
	}

	private AnimationMode suitableAnimationMode() {
		if (ghost.is(GhostState.DEAD) && ghost.killIndex != -1) {
			return AnimationMode.NUMBER_CUBE;
		}
		if (ghost.is(GhostState.DEAD) || ghost.is(GhostState.ENTERING_HOUSE)) {
			return AnimationMode.EYES_ONLY;
		}
		return AnimationMode.COLORED;
	}

	public void update() {
		var suitableMode = suitableAnimationMode();
		if (animationMode != suitableMode) {
			setAnimationMode(suitableMode);
		}
		switch (animationMode) {
		case COLORED, FRIGHTENED, FLASHING, EYES_ONLY -> {
			bodyAnimation.move();
		}
		case NUMBER_CUBE -> {
		}
		}
	}

	public void setAnimationMode(AnimationMode animationMode) {
		this.animationMode = animationMode;
		switch (animationMode) {
		case COLORED -> {
			bodyAnimation.setShowSkin(true);
			getChildren().setAll(bodyAnimation.getRoot());
		}
		case FRIGHTENED -> {
			bodyAnimation.setShowSkin(true);
			bodyAnimation.setFrightened(true);
			getChildren().setAll(bodyAnimation.getRoot());
		}
		case FLASHING -> {
			bodyAnimation.setShowSkin(true);
			bodyAnimation.setFrightened(true);
			getChildren().setAll(bodyAnimation.getRoot());
		}
		case EYES_ONLY -> {
			bodyAnimation.setShowSkin(false);
			bodyAnimation.setFrightened(false);
			getChildren().setAll(bodyAnimation.getRoot());
		}
		case NUMBER_CUBE -> {
			numberCubeAnimation.setNumber(ghost.killIndex);
			// rotate such that number appears in right orientation
			setRotationAxis(Rotate.X_AXIS);
			setRotate(0);
			getChildren().setAll(numberCubeAnimation.getRoot());
		}
		}
	}

	public boolean isLookingFrightened() {
		return bodyAnimation.frightened;
	}

	public void playFlashingAnimation() {
		bodyAnimation.playFlashingAnimation();
	}

	public void playRevivalAnimation() {
		bodyAnimation.playRevivalAnimation();
	}
}