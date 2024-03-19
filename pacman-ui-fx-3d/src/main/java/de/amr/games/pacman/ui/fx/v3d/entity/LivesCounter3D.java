/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI;
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
import static de.amr.games.pacman.ui.fx.util.ResourceManager.coloredMaterial;
import static java.util.Objects.requireNonNull;

/**
 * Displays a Pac-Man shape for each live remaining.
 *
 * @author Armin Reichert
 */
public class LivesCounter3D {

    private static final double PILLAR_HEIGHT = 8.0;
    private static final double PLATE_RADIUS = 6.0;
    private static final double PLATE_THICKNESS = 1.0;

    public final BooleanProperty lightOnPy = new SimpleBooleanProperty(this, "lightOn", true);
    public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

    private final Group root = new Group();
    private final Group standsGroup = new Group();

    private final PhongMaterial pillarMaterial;
    private final PhongMaterial plateMaterial;

    private final List<Pac3D> pac3DList = new ArrayList<>();
    private final List<Animation> animations = new ArrayList<>();

    public LivesCounter3D(Supplier<Pac3D> pacShapeSupplier, boolean lookRight, int maxLives) {
        requireNonNull(pacShapeSupplier);

        pillarMaterial = coloredMaterial(Color.rgb(100, 100, 100));
        plateMaterial = coloredMaterial(Color.rgb(180, 180, 180));

        for (int i = 0; i < maxLives; ++i) {
            addStand(2 * i * TS);
            var pac3D = pacShapeSupplier.get();
            addPac(pac3D, i, lookRight);

            var pacRotation = new RotateTransition(Duration.seconds(20.0), pac3D.root());
            pacRotation.setAxis(Rotate.Z_AXIS);
            pacRotation.setByAngle(360);
            pacRotation.setInterpolator(Interpolator.LINEAR);
            pacRotation.setCycleCount(Animation.INDEFINITE);
            animations.add(pacRotation);
        }

        var light = new PointLight(Color.CORNFLOWERBLUE);
        light.setMaxRange(TS * (maxLives + 1));
        light.setTranslateX(TS * (maxLives - 1));
        light.setTranslateY(TS * (-1));
        light.setTranslateZ(-PILLAR_HEIGHT - 20);
        light.lightOnProperty().bind(lightOnPy);

        root.getChildren().addAll(standsGroup, light);
    }

    public void addPac(Pac3D pac3D, int position, boolean lookRight) {
        double radius = pac3D.root().getBoundsInLocal().getHeight() / 2f;
        pac3D.position().setX(2 * position * TS);
        pac3D.position().setZ(-(PILLAR_HEIGHT + PLATE_THICKNESS + radius));
        if (lookRight) {
            pac3D.root().setRotationAxis(Rotate.Z_AXIS);
            pac3D.root().setRotate(180);
        }
        pac3DList.add(pac3D);
        root.getChildren().add(pac3D.root());
    }

    public void startAnimation() {
        animations.forEach(Animation::play);
    }

    public void stopAnimation() {
        animations.forEach(Animation::stop);
    }

    private void addStand(double x) {
        var plate = new Cylinder(PLATE_RADIUS, PLATE_THICKNESS);
        plate.setMaterial(plateMaterial);
        plate.setTranslateX(x);
        plate.setTranslateZ(-PILLAR_HEIGHT - PLATE_THICKNESS);
        plate.setRotationAxis(Rotate.X_AXIS);
        plate.setRotate(90);
        plate.drawModeProperty().bind(PacManGames3dUI.PY_3D_DRAW_MODE);

        var pillar = new Cylinder(1, PILLAR_HEIGHT);
        pillar.setMaterial(pillarMaterial);
        pillar.setTranslateX(x);
        pillar.setTranslateZ(-0.5 * PILLAR_HEIGHT);
        pillar.setRotationAxis(Rotate.X_AXIS);
        pillar.setRotate(90);
        pillar.drawModeProperty().bind(PacManGames3dUI.PY_3D_DRAW_MODE);

        standsGroup.getChildren().addAll(plate, pillar);
    }

    public Node root() {
        return root;
    }

    public void update(int numLives) {
        for (int i = 0; i < pac3DList.size(); ++i) {
            pac3DList.get(i).root().setVisible(i < numLives);
        }
    }
}