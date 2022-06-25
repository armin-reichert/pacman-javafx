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

import de.amr.games.pacman.ui.fx._3d.animation.ColorFlashingTransition;
import de.amr.games.pacman.ui.fx._3d.animation.Rendering3D;
import de.amr.games.pacman.ui.fx._3d.model.Model3D;
import javafx.animation.Animation.Status;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;

/**
 * @author Armin Reichert
 */
public class GhostBodyAnimation3D {

	private final Model3D model3D;
	private final int ghostID;
	private final Group ghostGroup;
	private final ColorFlashingTransition flashingAnimation;

	public GhostBodyAnimation3D(Model3D model3D, int ghostID) {
		this.model3D = model3D;
		this.ghostID = ghostID;
		ghostGroup = model3D.createGhost(//
				faded(Rendering3D.getGhostSkinColor(ghostID)), //
				Rendering3D.getGhostEyeBallColor(), //
				Rendering3D.getGhostPupilColor());
		flashingAnimation = new ColorFlashingTransition(//
				Rendering3D.getGhostSkinColorFrightened(), //
				Rendering3D.getGhostSkinColorFrightened2());
	}

	public Node getRoot() {
		return ghostGroup;
	}

	public void setShowBody(boolean showSkin) {
		model3D.ghostSkin(ghostGroup).setVisible(showSkin);
	}

	public void playFlashingAnimation() {
		if (flashingAnimation.getStatus() != Status.RUNNING) {
			model3D.ghostSkin(ghostGroup).setMaterial(flashingAnimation.getMaterial());
			flashingAnimation.playFromStart();
		}
	}

	public void ensureFlashingAnimationStopped() {
		if (flashingAnimation.getStatus() == Status.RUNNING) {
			flashingAnimation.stop();
		}
	}

	public void setFrightened(boolean frightened) {
		ensureFlashingAnimationStopped();
		if (frightened) {
			setShapeColor(model3D.ghostSkin(ghostGroup), faded(Rendering3D.getGhostSkinColorFrightened()));
			setShapeColor(model3D.ghostEyesBalls(ghostGroup), Rendering3D.getGhostEyeBallColorFrightened());
			setShapeColor(model3D.ghostEyesPupils(ghostGroup), Rendering3D.getGhostPupilColorFrightened());
		} else {
			setShapeColor(model3D.ghostSkin(ghostGroup), faded(Rendering3D.getGhostSkinColor(ghostID)));
			setShapeColor(model3D.ghostEyesBalls(ghostGroup), Rendering3D.getGhostEyeBallColor());
			setShapeColor(model3D.ghostEyesPupils(ghostGroup), Rendering3D.getGhostPupilColor());
		}
	}

	private void setShapeColor(Shape3D shape, Color diffuseColor) {
		var material = new PhongMaterial(diffuseColor);
		material.setSpecularColor(diffuseColor.brighter());
		shape.setMaterial(material);
	}

	private Color faded(Color color) {
		return Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.90);
	}
}