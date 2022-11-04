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

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx._3d.animation.ColorFlashing;
import de.amr.games.pacman.ui.fx._3d.animation.Rendering3D;
import de.amr.games.pacman.ui.fx._3d.model.Model3D;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.Animation.Status;
import javafx.animation.ParallelTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;

/**
 * @author Armin Reichert
 */
public class ColoredGhost3D {

	private final int ghostID;
	private final Model3D model3D;
	private final Group root3D;

	private final ObjectProperty<Color> dressColorPy;
	private final ObjectProperty<Color> eyeBallColorPy;
	private final ObjectProperty<Color> eyePupilColorPy;

	private final Color eyeBallColor;
	private final Color pupilColor;

	private final Color frightenedColor;
	private final Color frightenedEyeBallColor;
	private final Color frightenedPupilColor;

	private final Color flashingColor;
	private final Color flashingPupilColor;

	private ParallelTransition flashing;
	private ColorFlashing dressFlashing;
	private ColorFlashing pupilsFlashing;

	public ColoredGhost3D(int ghostID, Model3D model3D, Color dressColor, Color eyeBallColor, Color pupilColor,
			Color frightenedColor, Color frightenedEyeBallColor, Color frightenedPupilColor, Color flashingColor,
			Color flashingPupilColor) {

		this.ghostID = ghostID;
		this.model3D = model3D;

		this.eyeBallColor = eyeBallColor;
		this.pupilColor = pupilColor;

		this.frightenedColor = frightenedColor;
		this.frightenedEyeBallColor = frightenedEyeBallColor;
		this.frightenedPupilColor = frightenedPupilColor;

		this.flashingColor = flashingColor;
		this.flashingPupilColor = flashingPupilColor;

		root3D = model3D.createGhost(dressColor, eyeBallColor, pupilColor);

		dressColorPy = new SimpleObjectProperty<>(dressColor);
		var dressMaterial = new PhongMaterial();
		Ufx.bindMaterialColor(dressMaterial, dressColorPy);
		model3D.ghostDress(root3D).setMaterial(dressMaterial);

		eyeBallColorPy = new SimpleObjectProperty<>(eyeBallColor);
		var eyeBallMaterial = new PhongMaterial();
		Ufx.bindMaterialColor(eyeBallMaterial, eyeBallColorPy);
		model3D.ghostEyeBalls(root3D).setMaterial(eyeBallMaterial);

		eyePupilColorPy = new SimpleObjectProperty<>(pupilColor);
		var eyePupilMaterial = new PhongMaterial();
		Ufx.bindMaterialColor(eyePupilMaterial, eyePupilColorPy);
		model3D.ghostEyePupils(root3D).setMaterial(eyePupilMaterial);
	}

	private void createFlashing(int numFlashes) {
		var seconds = GameModel.PAC_POWER_FADING_TICKS / (2 * 60.0); // 2 animation cycles = 1 flashing
		dressFlashing = new ColorFlashing(frightenedColor, flashingColor, seconds, numFlashes);
		pupilsFlashing = new ColorFlashing(frightenedPupilColor, flashingPupilColor, seconds, numFlashes);
		flashing = new ParallelTransition(dressFlashing, pupilsFlashing);
	}

	public Shape3D dress() {
		return model3D.ghostDress(root3D);
	}

	public Node getRoot() {
		return root3D;
	}

	private void ensureFlashingPlaying(int numFlashes) {
		if (flashing == null) {
			createFlashing(numFlashes);
		}
		if (flashing.getStatus() != Status.RUNNING) {
			flashing.playFromStart();
		}
	}

	private void ensureFlashingStopped() {
		if (flashing != null && flashing.getStatus() == Status.RUNNING) {
			flashing.stop();
			flashing = null;
		}
	}

	public void wearBlueDress(int numFlashes) {
		if (numFlashes > 0) {
			ensureFlashingPlaying(numFlashes);
			dressColorPy.bind(dressFlashing.colorPy);
			eyePupilColorPy.bind(pupilsFlashing.colorPy);
			eyeBallColorPy.set(frightenedEyeBallColor);
		} else {
			ensureFlashingStopped();
			dressColorPy.unbind();
			eyePupilColorPy.unbind();
			dressColorPy.set(frightenedColor);
			eyePupilColorPy.set(frightenedPupilColor);
			eyeBallColorPy.set(frightenedEyeBallColor);
		}
		dress().setVisible(true);
	}

	public void wearColoredDress() {
		dressColorPy.unbind();
		eyePupilColorPy.unbind();
		ensureFlashingStopped();
		dressColorPy.set(Rendering3D.getGhostDressColor(ghostID));
		eyeBallColorPy.set(eyeBallColor);
		eyePupilColorPy.set(pupilColor);
		dress().setVisible(true);
	}
}