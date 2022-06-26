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

import java.util.function.Supplier;

import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.animation.FillTransition3D;
import de.amr.games.pacman.ui.fx._3d.animation.PortalAppearance;
import de.amr.games.pacman.ui.fx._3d.model.Model3D;
import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.binding.Bindings;
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
	private final Motion motion = new Motion();
	private final ObjectProperty<Color> pyFaceColor;
	private final Supplier<Color> fnNormalFaceColor;
	private final PhongMaterial faceMaterial;
	private final PortalAppearance portalAppearance;

	public Pac3D(Pac pac, Model3D model3D, Color faceColor, Color eyesColor, Color palateColor) {
		this.pac = pac;
		this.model3D = model3D;
		root3D = model3D.createPac(faceColor, eyesColor, palateColor);
		pyFaceColor = new SimpleObjectProperty<>(faceColor);
		faceMaterial = new PhongMaterial();
		faceMaterial.diffuseColorProperty().bind(pyFaceColor);
		faceMaterial.specularColorProperty()
				.bind(Bindings.createObjectBinding(() -> pyFaceColor.get().brighter(), pyFaceColor));
		face().setMaterial(faceMaterial);
		var light = new PointLight(Color.GHOSTWHITE);
		light.setTranslateZ(-8);
		getChildren().addAll(root3D, light);
		fnNormalFaceColor = () -> faceColor;
		portalAppearance = new PortalAppearance(pyFaceColor, fnNormalFaceColor);
	}

	public void reset(World world) {
		root3D.setScaleX(1.0);
		root3D.setScaleY(1.0);
		root3D.setScaleZ(1.0);
		update(world);
		// without this, the initial color is not always correct. Why?
		face().setMaterial(faceMaterial);
	}

	public void update(World world) {
		motion.update(pac, this);
		portalAppearance.update(this, pac, world);
	}

	public Animation dyingAnimation(Color ghostColor) {
		var spin = new RotateTransition(Duration.seconds(0.2), root3D);
		spin.setAxis(Rotate.Z_AXIS);
		spin.setByAngle(360);
		spin.setCycleCount(10);

		var shrink = new ScaleTransition(Duration.seconds(2), root3D);
		shrink.setToX(0.1);
		shrink.setToY(0.1);
		shrink.setToZ(0.1);

		return new SequentialTransition( //
				new FillTransition3D(Duration.seconds(1), face(), fnNormalFaceColor.get(), ghostColor), //
				new FillTransition3D(Duration.seconds(1), face(), ghostColor, Color.GHOSTWHITE), //
				new ParallelTransition(spin, shrink));
	}

	private Shape3D face() {
		return model3D.pacFace(root3D);
	}
}