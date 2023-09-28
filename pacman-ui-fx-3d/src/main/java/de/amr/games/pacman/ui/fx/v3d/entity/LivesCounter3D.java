/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.v3d.PacManGames3dApp;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static de.amr.games.pacman.lib.Globals.TS;
import static java.util.Objects.requireNonNull;

/**
 * Displays a Pac-Man shape for each live remaining.
 * 
 * @author Armin Reichert
 */
public class LivesCounter3D {

	public static LivesCounter3D counterPacManGame(Model3D model3D, Theme theme) {
		return new LivesCounter3D(() -> Pac3D.createPacManGroup(model3D, theme), false);
	}

	public static LivesCounter3D counterMsPacManGame(Model3D model3D, Theme theme) {
		return new LivesCounter3D(() -> Pac3D.createMsPacManGroup(model3D, theme), true);
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

	private LivesCounter3D(Supplier<Node> pacShapeProducer, boolean lookRight) {
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
			Model3D.meshView(pacShape, Pac3D.MESH_ID_HEAD).drawModeProperty().bind(PacManGames3dApp.PY_3D_DRAW_MODE);
			Model3D.meshView(pacShape, Pac3D.MESH_ID_EYES).drawModeProperty().bind(PacManGames3dApp.PY_3D_DRAW_MODE);
			Model3D.meshView(pacShape, Pac3D.MESH_ID_PALATE).drawModeProperty().bind(PacManGames3dApp.PY_3D_DRAW_MODE);

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
		plate.drawModeProperty().bind(PacManGames3dApp.PY_3D_DRAW_MODE);

		var pillar = new Cylinder(1, pillarHeight);
		pillar.setMaterial(pillarMaterial);
		pillar.setTranslateX(x);
		pillar.setTranslateZ(-0.5 * pillarHeight);
		pillar.setRotationAxis(Rotate.X_AXIS);
		pillar.setRotate(90);
		pillar.drawModeProperty().bind(PacManGames3dApp.PY_3D_DRAW_MODE);

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