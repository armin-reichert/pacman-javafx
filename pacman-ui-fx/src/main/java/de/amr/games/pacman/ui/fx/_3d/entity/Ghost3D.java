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

import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._3d.animation.ColorFlashingTransition;
import de.amr.games.pacman.ui.fx._3d.animation.FadeInTransition3D;
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
 * 3D representation of a ghost. A ghost is displayed in one of 3 modes: as a full ghost, as eyes only or as a bonus
 * symbol indicating the bounty paid for killing the ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost3D extends Group {

	private enum DisplayMode {
		COMPLETE_BODY, EYES_ONLY, NUMBER_CUBE
	}

	public final Ghost ghost;
	private final Group bodyParts;
	private final Box numberCube = new Box(8, 8, 8);
	private final Creature3DMotion<Ghost> motion;
	private final Rendering2D r2D;
	private final ColorFlashingTransition skinFlashing;

	private DisplayMode displayMode;

	public Ghost3D(Ghost ghost, PacManModel3D model3D, Rendering2D r2D) {
		this.ghost = ghost;
		this.r2D = r2D;

		skinFlashing = new ColorFlashingTransition(r2D.getGhostSkinColorFrightened(), r2D.getGhostSkinColorFrightened2());

		bodyParts = model3D.createGhost(r2D.getGhostSkinColor(ghost.id), r2D.getGhostEyeBallColor(),
				r2D.getGhostPupilColor());

		motion = new Creature3DMotion<Ghost>(ghost, this);
		getChildren().addAll(bodyParts, numberCube);
		reset();

		skin().setUserData(this);
		eyes().setUserData(this);
		eyeBalls().setUserData(this);
		pupils().setUserData(this);
	}

	public void reset() {
		setNormalColor();
		update();
	}

	public void update() {
		if (ghost.bounty > 0) {
			enterDisplayMode(DisplayMode.NUMBER_CUBE);
		} else if (ghost.is(GhostState.DEAD) || ghost.is(GhostState.ENTERING_HOUSE)) {
			enterDisplayMode(DisplayMode.EYES_ONLY);
			motion.update();
		} else {
			enterDisplayMode(DisplayMode.COMPLETE_BODY);
			motion.update();
		}
		boolean insideWorld = ghost.position.x >= 0 && ghost.position.x <= t(ghost.world.numCols() - 1);
		bodyParts.setVisible(ghost.visible && insideWorld);
	}

	public Shape3D skin() {
		return (Shape3D) bodyParts.getChildren().get(0);
	}

	/**
	 * @return group representing eyes = (pupils, eyeBalls)
	 */
	public Group eyes() {
		return (Group) bodyParts.getChildren().get(1);
	}

	private Shape3D pupils() {
		return (Shape3D) eyes().getChildren().get(0);
	}

	private Shape3D eyeBalls() {
		return (Shape3D) eyes().getChildren().get(1);
	}

	public String identifyNode(Node node) {
		if (node == eyeBalls()) {
			return String.format("eyeballs of %s", ghost);
		} else if (node == pupils()) {
			return String.format("pupils of %s", ghost);
		} else if (node == skin()) {
			return String.format("skin of %s", ghost);
		} else {
			return String.format("part of %s", ghost);
		}
	}

	private void enterDisplayMode(DisplayMode newMode) {
		if (displayMode == newMode) {
			return;
		}
		displayMode = newMode;
		switch (displayMode) {
		case COMPLETE_BODY -> {
			numberCube.setVisible(false);
			skin().setVisible(true);
			eyes().setVisible(true);
		}
		case EYES_ONLY -> {
			numberCube.setVisible(false);
			skin().setVisible(false);
			eyes().setVisible(true);
		}
		case NUMBER_CUBE -> {
			Image texture = r2D.spritesheet().extractRegion(r2D.getBountyNumberSprite(ghost.bounty));
			PhongMaterial material = new PhongMaterial();
			material.setBumpMap(texture);
			material.setDiffuseMap(texture);
			numberCube.setMaterial(material);
			// rotate such that number appears in right orientation
			setRotationAxis(Rotate.X_AXIS);
			setRotate(0);
			numberCube.setVisible(true);
			skin().setVisible(false);
			eyes().setVisible(false);
		}
		}
	}

	public void playFlashingAnimation() {
		skin().setMaterial(skinFlashing.getMaterial());
		skinFlashing.playFromStart();
	}

	public void playRevivalAnimation() {
		new FadeInTransition3D(Duration.seconds(1.5), skin(), r2D.getGhostSkinColor(ghost.id)).playFromStart();
	}

	public void setNormalColor() {
		setShapeColor(skin(), r2D.getGhostSkinColor(ghost.id));
		setShapeColor(eyeBalls(), r2D.getGhostEyeBallColor());
		setShapeColor(pupils(), r2D.getGhostPupilColor());
		skinFlashing.stop();
	}

	public void setFrightenedColor() {
		setShapeColor(skin(), r2D.getGhostSkinColorFrightened());
		setShapeColor(eyeBalls(), r2D.getGhostEyeBallColorFrightened());
		setShapeColor(pupils(), r2D.getGhostPupilColorFrightened());
		skinFlashing.stop();
	}

	private void setShapeColor(Shape3D shape, Color diffuseColor) {
		PhongMaterial material = new PhongMaterial(diffuseColor);
		material.setSpecularColor(diffuseColor.brighter());
		shape.setMaterial(material);
	}
}