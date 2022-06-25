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
	private final Group pacGroup;
	private final Motion motion = new Motion();
	private final ObjectProperty<Color> skullColorProperty = new SimpleObjectProperty<>();
	private final ObjectProperty<Color> normalSkullColorProperty = new SimpleObjectProperty<>();
	private final PhongMaterial skullMaterial = new PhongMaterial();
	private final PortalApproachAnimation portalApproachAnimation;

	public Pac3D(Pac pac, Model3D model3D, Color skullColor, Color eyesColor, Color palateColor) {
		this.pac = pac;
		this.model3D = model3D;
		skullColorProperty.set(skullColor);
		skullMaterial.diffuseColorProperty().bind(skullColorProperty);
		skullMaterial.specularColorProperty()
				.bind(Bindings.createObjectBinding(() -> skullColorProperty.get().brighter(), skullColorProperty));
		pacGroup = model3D.createPac(skullColor, eyesColor, palateColor);
		skull().setMaterial(skullMaterial);
		var light = new PointLight(Color.WHITE);
		light.setTranslateZ(-8);
		getChildren().addAll(pacGroup, light);
		normalSkullColorProperty.set(skullColor);
		portalApproachAnimation = new PortalApproachAnimation(skullColorProperty, normalSkullColorProperty::get);
	}

	private Shape3D skull() {
		return model3D.pacSkull(pacGroup);
	}

	public void reset(World world) {
		pacGroup.setScaleX(1.0);
		pacGroup.setScaleY(1.0);
		pacGroup.setScaleZ(1.0);
		update(world);
	}

	public void update(World world) {
		motion.update(pac, this);
		portalApproachAnimation.update(this, pac, world);
	}

	public Animation dyingAnimation(Color ghostColor) {
		var spin = new RotateTransition(Duration.seconds(0.2), pacGroup);
		spin.setAxis(Rotate.Z_AXIS);
		spin.setByAngle(360);
		spin.setCycleCount(10);

		var shrink = new ScaleTransition(Duration.seconds(2), pacGroup);
		shrink.setToX(0);
		shrink.setToY(0);
		shrink.setToZ(0);

		return new SequentialTransition( //
				new FillTransition3D(Duration.seconds(1), skull(), normalSkullColorProperty.get(), ghostColor), //
				new FillTransition3D(Duration.seconds(1), skull(), ghostColor, Color.GHOSTWHITE), //
				new ParallelTransition(spin, shrink));
	}
}