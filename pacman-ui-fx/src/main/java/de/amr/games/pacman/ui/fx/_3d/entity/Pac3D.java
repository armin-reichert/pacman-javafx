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

import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.animation.FillTransition3D;
import de.amr.games.pacman.ui.fx._3d.animation.Motion;
import de.amr.games.pacman.ui.fx._3d.animation.PortalAppearance;
import de.amr.games.pacman.ui.fx._3d.model.Model3D;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 3D-representation of Pac-Man or Ms. Pac-Man.
 * 
 * <p>
 * Missing: Specific 3D-model for Ms. Pac-Man, mouth animation...
 * 
 * @author Armin Reichert
 */
public class Pac3D extends Group {

	private final Pac pac;
	private final Model3D model3D;
	private final Group root3D;
	private final Motion motion;
	private final ObjectProperty<Color> pyFaceColor;
	private final Color normalFaceColor;
	private final PhongMaterial faceMaterial;
	private final PortalAppearance portalAppearance;

	public Pac3D(Pac pac, Model3D model3D, Color faceColor, Color eyesColor, Color palateColor) {
		this.pac = pac;
		this.model3D = model3D;
		normalFaceColor = faceColor;
		pyFaceColor = new SimpleObjectProperty<>(faceColor);
		faceMaterial = new PhongMaterial();
		Ufx.bindMaterialColor(faceMaterial, pyFaceColor);
		root3D = model3D.createPac(faceColor, eyesColor, palateColor);
		face().setMaterial(faceMaterial);
		var light = new PointLight(Color.GHOSTWHITE);
		light.setTranslateZ(-6);
		getChildren().addAll(root3D, light);
		motion = new Motion();
		portalAppearance = new PortalAppearance(pyFaceColor, () -> normalFaceColor);
	}

	public void reset(World world) {
		root3D.setScaleX(1.0);
		root3D.setScaleY(1.0);
		root3D.setScaleZ(1.0);
		root3D.setTranslateZ(0);
		update(world);
		// without this, the initial color is not always correct. Why?
		face().setMaterial(faceMaterial);
	}

	public void update(World world) {
		motion.update(pac, this);
		portalAppearance.update(this, pac, world);
	}

	/**
	 * @param ghostColor color of ghost that killed Pac-Man
	 * @return dying animation (must not be longer than time reserved by game controller which is 5 seconds!)
	 */
	public Animation createDyingAnimation(Color ghostColor) {

		var colorChangingTime = Duration.seconds(2);
		var poisened = new FillTransition3D(colorChangingTime.multiply(0.9), face(), normalFaceColor, ghostColor);
		var impaling = new FillTransition3D(colorChangingTime.multiply(0.1), face(), ghostColor, Color.GHOSTWHITE);

		var collapsingTime = Duration.seconds(1.8);
		var numSpins = 10;

		var spinning = new RotateTransition(collapsingTime.divide(numSpins), root3D);
		spinning.setAxis(Rotate.Z_AXIS);
		spinning.setByAngle(360);
		spinning.setCycleCount(numSpins);
		spinning.setInterpolator(Interpolator.EASE_OUT);

		var shrinking = new ScaleTransition(collapsingTime, root3D);
		shrinking.setToX(0.8);
		shrinking.setToY(0.8);
		shrinking.setToZ(0.0);

		var sinking = new TranslateTransition(collapsingTime, root3D);
		sinking.setFromZ(0);
		sinking.setToZ(World.HTS);

		return new SequentialTransition(poisened, impaling, new ParallelTransition(spinning, shrinking, sinking));
	}

	private Shape3D face() {
		return model3D.pacFace(root3D);
	}
}