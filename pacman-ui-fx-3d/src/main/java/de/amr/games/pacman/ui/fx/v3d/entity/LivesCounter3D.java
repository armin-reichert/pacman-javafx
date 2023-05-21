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
package de.amr.games.pacman.ui.fx.v3d.entity;

import static de.amr.games.pacman.lib.Globals.TS;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import de.amr.games.pacman.ui.fx.rendering2d.MsPacManColoring;
import de.amr.games.pacman.ui.fx.rendering2d.PacManColoring;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.v3d.app.PacManGames3d;
import de.amr.games.pacman.ui.fx.v3d.model.Model3D;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

	public static LivesCounter3D of(PacManColoring colors) {
		return new LivesCounter3D(() -> Pac3D.createPacManGroup(PacManGames3d.assets.pacModel3D, colors), false);
	}

	public static LivesCounter3D of(MsPacManColoring colors) {
		return new LivesCounter3D(() -> Pac3D.createMsPacManGroup(PacManGames3d.assets.pacModel3D, colors), true);
	}

	public final BooleanProperty lightOnPy = new SimpleBooleanProperty(this, "lightOn", true);
	public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

	private final Group root = new Group();
	private final Group pacShapesGroup = new Group();
	private final Group standsGroup = new Group();
	private final PointLight light;

	private double pillarHeight = 5.0;
	private final PhongMaterial pillarMaterial;

	private double plateRadius = 6.0;
	private double plateThickness = 1.0;
	private final PhongMaterial plateMaterial;

	private final List<Animation> animations = new ArrayList<>();

	private LivesCounter3D(Supplier<Group> pacShapeProducer, boolean lookRight) {
		requireNonNull(pacShapeProducer);

		pillarMaterial = ResourceManager.coloredMaterial(Color.rgb(100, 100, 100));
		plateMaterial = ResourceManager.coloredMaterial(Color.rgb(180, 180, 180));

		int maxLives = 5;
		for (int i = 0; i < maxLives; ++i) {
			addStand(2 * i * TS);

			var pacShape = pacShapeProducer.get();
			pacShape.setTranslateX(2.0 * i * TS);
			pacShape.setTranslateZ(-(pillarHeight + 5.5));
			if (lookRight) {
				pacShape.setRotationAxis(Rotate.Z_AXIS);
				pacShape.setRotate(180);
			}
			Model3D.meshView(pacShape, Pac3D.MESH_ID_HEAD).drawModeProperty().bind(PacManGames3d.PY_3D_DRAW_MODE);
			Model3D.meshView(pacShape, Pac3D.MESH_ID_EYES).drawModeProperty().bind(PacManGames3d.PY_3D_DRAW_MODE);
			Model3D.meshView(pacShape, Pac3D.MESH_ID_PALATE).drawModeProperty().bind(PacManGames3d.PY_3D_DRAW_MODE);

			var plateRotation = new RotateTransition(Duration.seconds(20.0), pacShape);
			plateRotation.setAxis(Rotate.Z_AXIS);
			plateRotation.setByAngle(360);
			plateRotation.setInterpolator(Interpolator.LINEAR);
			plateRotation.setCycleCount(Animation.INDEFINITE);
			animations.add(plateRotation);

			pacShapesGroup.getChildren().add(pacShape);
		}

		light = new PointLight(Color.CORNFLOWERBLUE);
		light.setMaxRange(TS * (maxLives + 1));
		light.setTranslateX(TS * (maxLives - 1));
		light.setTranslateY(TS * (-1));
		light.setTranslateZ(-pillarHeight - 20);
		light.lightOnProperty().bind(lightOnPy);

		root.getChildren().addAll(standsGroup, pacShapesGroup, light);
	}

	public void startAnimation() {
		animations.forEach(Animation::play);
	}

	public void stopAnimation() {
		animations.forEach(Animation::stop);
	}

	private void addStand(double x) {
		var plate = new Cylinder(plateRadius, plateThickness);
		plate.setMaterial(plateMaterial);
		plate.setTranslateX(x);
		plate.setTranslateZ(-pillarHeight - plateThickness);
		plate.setRotationAxis(Rotate.X_AXIS);
		plate.setRotate(90);
		plate.drawModeProperty().bind(PacManGames3d.PY_3D_DRAW_MODE);

		var pillar = new Cylinder(1, pillarHeight);
		pillar.setMaterial(pillarMaterial);
		pillar.setTranslateX(x);
		pillar.setTranslateZ(-0.5 * pillarHeight);
		pillar.setRotationAxis(Rotate.X_AXIS);
		pillar.setRotate(90);
		pillar.drawModeProperty().bind(PacManGames3d.PY_3D_DRAW_MODE);

		standsGroup.getChildren().addAll(plate, pillar);
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
		for (int i = 0; i < pacShapesGroup.getChildren().size(); ++i) {
			var node = pacShapesGroup.getChildren().get(i);
			node.setVisible(i < numLives);
		}
	}
}