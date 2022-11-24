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
import de.amr.games.pacman.ui.fx._2d.rendering.GhostColorScheme;
import de.amr.games.pacman.ui.fx._3d.animation.ColorFlashing;
import de.amr.games.pacman.ui.fx._3d.model.Model3D;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.Animation.Status;
import javafx.animation.ParallelTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape3D;

/**
 * @author Armin Reichert
 */
public class ColoredGhost3D {

	private final Group ghostGroup;
	private final Shape3D dress;
	private final Shape3D eyeBalls;
	private final Shape3D pupils;

	private final GhostColorScheme colors;
	private final ObjectProperty<Color> dressColorPy;
	private final ObjectProperty<Color> eyeBallColorPy;
	private final ObjectProperty<Color> eyePupilColorPy;

	private ParallelTransition flashing;
	private ColorFlashing dressFlashing;
	private ColorFlashing pupilsFlashing;

	public ColoredGhost3D(GhostColorScheme colors) {
		this.colors = colors;
		dressColorPy = new SimpleObjectProperty<>(this, "dressColor", colors.normalDress());
		eyeBallColorPy = new SimpleObjectProperty<>(this, "eyeBallColor", colors.normalEyeBalls());
		eyePupilColorPy = new SimpleObjectProperty<>(this, "eyePupilColor", colors.normalPupils());
		ghostGroup = Model3D.createGhost(colors.normalDress(), colors.normalEyeBalls(), colors.normalPupils());
		dress = Model3D.ghostDress(ghostGroup);
		dress.setMaterial(Ufx.createColorBoundMaterial(dressColorPy));
		eyeBalls = Model3D.ghostEyeBalls(ghostGroup);
		eyeBalls.setMaterial(Ufx.createColorBoundMaterial(eyeBallColorPy));
		pupils = Model3D.ghostEyePupils(ghostGroup);
		pupils.setMaterial(Ufx.createColorBoundMaterial(eyePupilColorPy));
	}

	public Node root() {
		return ghostGroup;
	}

	public void appearFlashing(int numFlashes) {
		ensureFlashingPlaying(numFlashes);
		dressColorPy.bind(dressFlashing.colorPy);
		eyeBallColorPy.set(colors.frightenedEyeBalls());
		eyePupilColorPy.bind(pupilsFlashing.colorPy);
		dress.setVisible(true);
	}

	public void appearFrightened() {
		dressColorPy.unbind();
		dressColorPy.set(colors.frightenedDress());
		eyeBallColorPy.set(colors.frightenedEyeBalls());
		eyePupilColorPy.unbind();
		eyePupilColorPy.set(colors.frightendPupils());
		dress.setVisible(true);
		ensureFlashingStopped();
	}

	public void appearNormal() {
		dressColorPy.unbind();
		dressColorPy.set(colors.normalDress());
		eyeBallColorPy.set(colors.normalEyeBalls());
		eyePupilColorPy.unbind();
		eyePupilColorPy.set(colors.normalPupils());
		dress.setVisible(true);
		ensureFlashingStopped();
	}

	public void appearEyesOnly() {
		appearNormal();
		dress.setVisible(false);
	}

	private void createFlashing(int numFlashes) {
		var seconds = (double) GameModel.PAC_POWER_FADING_TICKS / (2 * GameModel.FPS); // 2 animation cycles = 1 flashing
		dressFlashing = new ColorFlashing(colors.frightenedDress(), colors.flashingDress(), seconds, numFlashes);
		pupilsFlashing = new ColorFlashing(colors.frightendPupils(), colors.flashingPupils(), seconds, numFlashes);
		flashing = new ParallelTransition(dressFlashing, pupilsFlashing);
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
}