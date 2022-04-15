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
public class Ghost3D extends Creature3D<Ghost> {

	private enum DisplayMode {
		COMPLETE, EYES_ONLY, NUMBER_CUBE
	}

	private final Rendering2D r2D;
	private final Group bodyParts;
	private final Box pointsCube3D = new Box(8, 8, 8);
	private final ColorFlashingTransition flashing = new ColorFlashingTransition(Color.color(0.5, 0.7, 1.0));

	private DisplayMode displayMode;
	private Color frightenedColor = Color.CORNFLOWERBLUE;

	public Ghost3D(Ghost ghost, PacManModel3D model3D, Rendering2D r2D) {
		super(ghost);
		this.r2D = r2D;
		bodyParts = model3D.createGhost(r2D.getGhostColor(ghost.id), Color.WHITE, Color.BLACK);
		bodyParts.setRotationAxis(Rotate.Z_AXIS);
		bodyParts.setRotate(turnAngle(ghost.moveDir()));
		turningAnimation.setNode(bodyParts);
		getChildren().addAll(bodyParts, pointsCube3D);
		reset();
	}

	public Shape3D skin() {
		return (Shape3D) bodyParts.getChildren().get(0);
	}

	public Node eyes() {
		return bodyParts.getChildren().get(1);
	}

	private void setDisplayMode(DisplayMode mode) {
		if (displayMode != mode) {
			displayMode = mode;
			pointsCube3D.setVisible(displayMode == DisplayMode.NUMBER_CUBE);
			skin().setVisible(displayMode == DisplayMode.COMPLETE);
			eyes().setVisible(displayMode == DisplayMode.COMPLETE || displayMode == DisplayMode.EYES_ONLY);
		}
	}

	public void reset() {
		setNormalSkinColor();
		update();
	}

	@Override
	public void update() {
		if (guy.bounty > 0) {
			if (displayMode != DisplayMode.NUMBER_CUBE) {
				setDisplayMode(DisplayMode.NUMBER_CUBE);
				PhongMaterial material = new PhongMaterial();
				Image image = r2D.spritesheet().extractRegion(r2D.getBountyNumberSprite(guy.bounty));
				material.setBumpMap(image);
				material.setDiffuseMap(image);
				pointsCube3D.setMaterial(material);
				setRotationAxis(Rotate.X_AXIS);
				setRotate(0);
			}
		} else if (guy.is(GhostState.DEAD) || guy.is(GhostState.ENTERING_HOUSE)) {
			setDisplayMode(DisplayMode.EYES_ONLY);
		} else {
			setDisplayMode(DisplayMode.COMPLETE);
		}
		super.update();
	}

	public void playFlashingAnimation() {
		skin().setMaterial(flashing.getMaterial());
		flashing.playFromStart();
	}

	public void playRevivalAnimation() {
		var animation = new FadeInTransition3D(Duration.seconds(2), skin(), r2D.getGhostColor(guy.id));
		animation.playFromStart();
	}

	public void setNormalSkinColor() {
		flashing.stop();
		setSkinColor(r2D.getGhostColor(guy.id));
	}

	public void setFrightenedSkinColor() {
		flashing.stop();
		setSkinColor(frightenedColor);
	}

	private void setSkinColor(Color color) {
		PhongMaterial material = new PhongMaterial(color);
		material.setSpecularColor(color.brighter());
		skin().setMaterial(material);
	}
}