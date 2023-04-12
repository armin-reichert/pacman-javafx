/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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

import static de.amr.games.pacman.model.common.world.World.TS;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * Displays a Pac-Man shape for each live remaining.
 * 
 * @author Armin Reichert
 */
public class LivesCounter3D {

	public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

	private final Group root = new Group();
	private final Group pacGroup = new Group();
	private final Group socketGroup = new Group();
	private final PointLight light;

	private final PhongMaterial plateauMaterial = ResourceMgr.coloredMaterial(Color.rgb(180, 180, 180));
	private final PhongMaterial pillarMaterial = ResourceMgr.coloredMaterial(Color.rgb(100, 100, 100));
	private double socketHeight = 5.0;
	private double plateauRadius = 6.0;
	private double plateauHeight = 1.0;

	private final List<Animation> animations = new ArrayList<>();

	public LivesCounter3D(int maxLives, Supplier<Group> fnPacShape, boolean lookRight) {
		for (int i = 0; i < maxLives; ++i) {
			addSocket(2 * i * TS, socketHeight);
			var pac = fnPacShape.get();
			pac.setTranslateX(2.0 * i * TS);
			pac.setTranslateZ(-socketHeight - 5.0);
			if (lookRight) {
				pac.setRotationAxis(Rotate.Z_AXIS);
				pac.setRotate(180);
			}
			pacGroup.getChildren().add(pac);

			var animation = new RotateTransition(Duration.seconds(20.0), pac);
			animation.setAxis(Rotate.Z_AXIS);
			animation.setByAngle(360);
			animation.setInterpolator(Interpolator.LINEAR);
			animation.setCycleCount(Animation.INDEFINITE);
			animations.add(animation);
		}

		light = new PointLight(Color.YELLOW);
		light.setTranslateX(TS * (maxLives - 1));
		light.setTranslateY(-TS);
		light.setTranslateZ(-socketHeight - 10);
		light.setMaxRange(5 * TS);

		root.getChildren().addAll(socketGroup, pacGroup, light);
	}

	public void startAnimation() {
		animations.forEach(Animation::play);
	}

	public void stopAnimation() {
		animations.forEach(Animation::stop);
	}

	private void addSocket(double x, double pillarHeight) {
		var plateau = new Cylinder(plateauRadius, plateauHeight);
		plateau.setMaterial(plateauMaterial);
		plateau.setTranslateX(x);
		plateau.setTranslateZ(-pillarHeight - plateauHeight);
		plateau.setRotationAxis(Rotate.X_AXIS);
		plateau.setRotate(90);
		plateau.drawModeProperty().bind(Env.d3_drawModePy);

		var pillar = new Cylinder(1, pillarHeight);
		pillar.setMaterial(pillarMaterial);
		pillar.setTranslateX(x);
		pillar.setTranslateZ(-0.5 * pillarHeight);
		pillar.setRotationAxis(Rotate.X_AXIS);
		pillar.setRotate(90);
		pillar.drawModeProperty().bind(Env.d3_drawModePy);

		socketGroup.getChildren().addAll(plateau, pillar);
	}

	public Node getRoot() {
		return root;
	}

	public void setPosition(double x, double y, double z) {
		root.setTranslateX(x);
		root.setTranslateY(y);
		root.setTranslateZ(z);
	}

	public void update(int numLives) {
		for (int i = 0; i < pacGroup.getChildren().size(); ++i) {
			var node = pacGroup.getChildren().get(i);
			node.setVisible(i < numLives);
		}
	}
}