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
import static de.amr.games.pacman.ui.fx.util.Ufx.requirePositive;
import static java.util.Objects.requireNonNull;

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
	private final Group pillarAndPlateGroup = new Group();
	private final PointLight light;

	private double pillarHeight = 5.0;
	private final PhongMaterial pillarMaterial = ResourceMgr.coloredMaterial(Color.rgb(100, 100, 100));

	private double plateRadius = 6.0;
	private double plateThickness = 1.0;
	private final PhongMaterial plateMaterial = ResourceMgr.coloredMaterial(Color.rgb(180, 180, 180));

	private final List<Animation> animations = new ArrayList<>();

	public LivesCounter3D(int maxLives, Supplier<Node> fnPacNode, boolean lookRight) {
		requirePositive(maxLives);
		requireNonNull(fnPacNode);

		for (int i = 0; i < maxLives; ++i) {
			addPillarAndPlate(2 * i * TS);
			var pacShape = fnPacNode.get();
			PacModel3D.headMeshView(pacShape).drawModeProperty().bind(Env.d3_drawModePy);
			PacModel3D.palateMeshView(pacShape).drawModeProperty().bind(Env.d3_drawModePy);
			PacModel3D.eyesMeshView(pacShape).drawModeProperty().bind(Env.d3_drawModePy);
			pacShape.setTranslateX(2.0 * i * TS);
			pacShape.setTranslateZ(-(pillarHeight + 5.5));
			if (lookRight) {
				pacShape.setRotationAxis(Rotate.Z_AXIS);
				pacShape.setRotate(180);
			}
			pacGroup.getChildren().add(pacShape);

			var plateRotation = new RotateTransition(Duration.seconds(20.0), pacShape);
			plateRotation.setAxis(Rotate.Z_AXIS);
			plateRotation.setByAngle(360);
			plateRotation.setInterpolator(Interpolator.LINEAR);
			plateRotation.setCycleCount(Animation.INDEFINITE);
			animations.add(plateRotation);
		}

		light = new PointLight(Color.grayRgb(200));
		light.setTranslateX(TS * (maxLives - 1));
		light.setTranslateY(-2 * TS);
		light.setTranslateZ(-pillarHeight - 10);
		light.setMaxRange(5 * TS);

		root.getChildren().addAll(pillarAndPlateGroup, pacGroup, light);
	}

	public void startAnimation() {
		animations.forEach(Animation::play);
	}

	public void stopAnimation() {
		animations.forEach(Animation::stop);
	}

	private void addPillarAndPlate(double x) {
		var plate = new Cylinder(plateRadius, plateThickness);
		plate.setMaterial(plateMaterial);
		plate.setTranslateX(x);
		plate.setTranslateZ(-pillarHeight - plateThickness);
		plate.setRotationAxis(Rotate.X_AXIS);
		plate.setRotate(90);
		plate.drawModeProperty().bind(Env.d3_drawModePy);

		var pillar = new Cylinder(1, pillarHeight);
		pillar.setMaterial(pillarMaterial);
		pillar.setTranslateX(x);
		pillar.setTranslateZ(-0.5 * pillarHeight);
		pillar.setRotationAxis(Rotate.X_AXIS);
		pillar.setRotate(90);
		pillar.drawModeProperty().bind(Env.d3_drawModePy);

		pillarAndPlateGroup.getChildren().addAll(plate, pillar);
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