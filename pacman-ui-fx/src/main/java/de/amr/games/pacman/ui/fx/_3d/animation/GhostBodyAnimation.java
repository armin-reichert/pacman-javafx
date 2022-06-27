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
package de.amr.games.pacman.ui.fx._3d.animation;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Ghost;
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
public class GhostBodyAnimation {

	private final Ghost ghost;
	private final Model3D model3D;
	private final Group root3D;

	private final MotionAnimation motion;

	private final ObjectProperty<Color> pyDressColor;
	private final ObjectProperty<Color> pyEyeBallsColor;
	private final ObjectProperty<Color> pyEyePupilsColor;

	private ParallelTransition flashing;
	private ColorFlashing dressFlashing;
	private ColorFlashing pupilsFlashing;

	public GhostBodyAnimation(Ghost ghost, Model3D model3D, MotionAnimation motion) {
		this.ghost = ghost;
		this.model3D = model3D;
		this.motion = motion;

		var dressColor = Rendering3D.getGhostDressColor(ghost.id);
		var eyeBallColor = Rendering3D.getGhostEyeBallColor();
		var pupilColor = Rendering3D.getGhostPupilColorBlue();

		root3D = model3D.createGhost(dressColor, eyeBallColor, pupilColor);

		pyDressColor = new SimpleObjectProperty<>(dressColor);
		var dressMaterial = new PhongMaterial();
		Ufx.bindMaterialColor(dressMaterial, pyDressColor);
		dress().setMaterial(dressMaterial);

		pyEyeBallsColor = new SimpleObjectProperty<>(Rendering3D.getGhostEyeBallColor());
		var eyeBallsMaterial = new PhongMaterial();
		Ufx.bindMaterialColor(eyeBallsMaterial, pyEyeBallsColor);
		eyeBalls().setMaterial(eyeBallsMaterial);

		pyEyePupilsColor = new SimpleObjectProperty<>(Rendering3D.getGhostPupilColorBlue());
		var eyePupilsMaterial = new PhongMaterial();
		Ufx.bindMaterialColor(eyePupilsMaterial, pyEyePupilsColor);
		eyePupils().setMaterial(eyePupilsMaterial);
	}

	private void createFlashing(int numFlashes) {
		var seconds = GameModel.PAC_POWER_FADING_TICKS / (2 * 60.0); // 2 animation cycles = 1 flashing
		dressFlashing = new ColorFlashing(Rendering3D.getGhostDressColorBlue(), Rendering3D.getGhostDressColorFlashing(),
				seconds, numFlashes);
		pupilsFlashing = new ColorFlashing(Rendering3D.getGhostPupilColorPink(), Rendering3D.getGhostPupilColorRed(),
				seconds, numFlashes);
		flashing = new ParallelTransition(dressFlashing, pupilsFlashing);
	}

	public void reset() {
		motion.reset();
	}

	public void update() {
		motion.update();
	}

	public Shape3D dress() {
		return model3D.ghostDress(root3D);
	}

	private Shape3D eyeBalls() {
		return model3D.ghostEyeBalls(root3D);
	}

	private Shape3D eyePupils() {
		return model3D.ghostEyePupils(root3D);
	}

	public Node getRoot() {
		return root3D;
	}

	private void flash(int numFlashes) {
		if (flashing == null) {
			createFlashing(numFlashes);
		}
		if (flashing.getStatus() != Status.RUNNING) {
			pyDressColor.bind(dressFlashing.pyColor);
			pyEyePupilsColor.bind(pupilsFlashing.pyColor);
			flashing.playFromStart();
		}
	}

	private void noFlash() {
		if (flashing != null && flashing.getStatus() == Status.RUNNING) {
			pyDressColor.unbind();
			pyEyePupilsColor.unbind();
			flashing.stop();
			flashing = null;
		}
	}

	public void wearBlueDress(int numFlashes) {
		dress().setVisible(true);
		if (numFlashes > 0) {
			flash(numFlashes);
		} else {
			noFlash();
		}
		pyDressColor.set(Rendering3D.getGhostDressColorBlue());
		pyEyeBallsColor.set(Rendering3D.getGhostEyeBallColorFrightened());
		pyEyePupilsColor.set(Rendering3D.getGhostPupilColorRed());
	}

	public void wearColoredDress() {
		dress().setVisible(true);
		noFlash();
		pyDressColor.set(Rendering3D.getGhostDressColor(ghost.id));
		pyEyeBallsColor.set(Rendering3D.getGhostEyeBallColor());
		pyEyePupilsColor.set(Rendering3D.getGhostPupilColorBlue());
	}
}