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
import de.amr.games.pacman.ui.fx._3d.animation.BlueFlashingAnimation;
import de.amr.games.pacman.ui.fx._3d.model.PacManModel3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;

/**
 * 3D-representation of a ghost. A ghost is displayed in one of 3 modes: as a full ghost, as eyes only or as a bonus
 * symbol indicating the bounty paid for killing the ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost3D extends Creature3D {

	private enum DisplayMode {
		COMPLETE, EYES_ONLY, NUMBER_CUBE
	}

	public final Ghost ghost;

	private final Rendering2D r2D;
	private final Shape3D skin3D;
	private final Node eyes3D;
	private final Box cube3D;
	private final BlueFlashingAnimation flashing = new BlueFlashingAnimation();

	private DisplayMode displayMode;
	private Color frightenedColor = Color.CORNFLOWERBLUE;

	public Ghost3D(Ghost ghost, PacManModel3D model3D, Rendering2D r2D) {
		this.ghost = ghost;
		this.r2D = r2D;
		Group body3D = model3D.createGhost(r2D.getGhostColor(ghost.id), Color.WHITE, Color.BLACK);
		skin3D = (Shape3D) body3D.getChildren().get(0);
		eyes3D = body3D.getChildren().get(1);
		cube3D = new Box(8, 8, 8);
		getChildren().addAll(body3D, cube3D);
		body3D.setRotationAxis(Rotate.Z_AXIS);
		body3D.setRotate(turnAngle(ghost.dir()));
		turningAnimation.setNode(body3D);
		targetDir = ghost.dir();
		reset();
	}

	private void setDisplayMode(DisplayMode mode) {
		if (displayMode != mode) {
			displayMode = mode;
			cube3D.setVisible(displayMode == DisplayMode.NUMBER_CUBE);
			skin3D.setVisible(displayMode == DisplayMode.COMPLETE);
			eyes3D.setVisible(displayMode == DisplayMode.COMPLETE || displayMode == DisplayMode.EYES_ONLY);
		}
	}

	public void reset() {
		setNormalSkinColor();
		update();
	}

	@Override
	public void update() {
		if (ghost.bounty > 0) {
			if (displayMode != DisplayMode.NUMBER_CUBE) {
				setDisplayMode(DisplayMode.NUMBER_CUBE);
				PhongMaterial material = new PhongMaterial();
				Image image = r2D.extractRegion(r2D.getBountyNumberSprites().get(ghost.bounty));
				material.setBumpMap(image);
				material.setDiffuseMap(image);
				cube3D.setMaterial(material);
				setRotationAxis(Rotate.X_AXIS);
				setRotate(0);
			}
		} else if (ghost.is(GhostState.DEAD) || ghost.is(GhostState.ENTERING_HOUSE)) {
			setDisplayMode(DisplayMode.EYES_ONLY);
		} else {
			setDisplayMode(DisplayMode.COMPLETE);
		}
		update(ghost);
	}

	public void playFlashingAnimation() {
		skin3D.setMaterial(flashing.getMaterial());
		flashing.playFromStart();
	}

	public void setNormalSkinColor() {
		flashing.stop();
		setSkinColor(r2D.getGhostColor(ghost.id));
	}

	public void setFrightenedSkinColor() {
		flashing.stop();
		setSkinColor(frightenedColor);
	}

	private void setSkinColor(Color color) {
		PhongMaterial material = new PhongMaterial(color);
		material.setSpecularColor(color.brighter());
		skin3D.setMaterial(material);
	}
}