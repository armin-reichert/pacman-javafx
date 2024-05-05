/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
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
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui.fx.util.ResourceManager.coloredMaterial;

/**
 * Displays a Pac-Man shape for each live remaining.
 *
 * @author Armin Reichert
 */
public class LivesCounter3D extends Group {

    public final BooleanProperty lightOnPy = new SimpleBooleanProperty(this, "lightOn", true);
    public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

    private final Group standsGroup = new Group();

    private final PhongMaterial pillarMaterial;
    private final PhongMaterial plateMaterial;

    private final List<Pac3D> pac3DList = new ArrayList<>();
    private final List<Animation> animations = new ArrayList<>();

    private final int maxLives;
    private final double pillarHeight;
    private final double plateRadius;
    private final double plateThickness;

    public LivesCounter3D(int maxLives,
                          Color pillarColor, double pillarHeight,
                          Color plateColor, double plateThickness, double plateRadius,
                          Color lightColor) {
        this.maxLives = maxLives;
        this.pillarHeight = pillarHeight;
        this.plateThickness = plateThickness;
        this.plateRadius = plateRadius;
        pillarMaterial = coloredMaterial(pillarColor);
        plateMaterial = coloredMaterial(plateColor);

        var light = new PointLight(lightColor);
        light.setMaxRange  (TS * (maxLives + 1));
        light.setTranslateX(TS * (maxLives - 1));
        light.setTranslateY(TS * (-1));
        light.setTranslateZ(-(pillarHeight + 20));
        light.lightOnProperty().bind(lightOnPy);

        getChildren().addAll(standsGroup, light);
    }

    public void addItem(Pac3D pac3D, boolean lookRight) {
        int x = pac3DList.size() * 2 * TS;
        addStand(x);
        double radius = pac3D.getBoundsInLocal().getHeight() / 2f;
        pac3D.position().setX(x);
        pac3D.position().setZ(-(pillarHeight + plateThickness + radius));
        if (lookRight) {
            pac3D.setRotationAxis(Rotate.Z_AXIS);
            pac3D.setRotate(180);
        }
        pac3D.drawModePy.bind(drawModePy);
        pac3DList.add(pac3D);
        getChildren().add(pac3D);

        var rotation = new RotateTransition(Duration.seconds(20.0), pac3D);
        rotation.setAxis(Rotate.Z_AXIS);
        rotation.setByAngle(180);
        rotation.setInterpolator(Interpolator.LINEAR);
        rotation.setCycleCount(Animation.INDEFINITE);
        rotation.setAutoReverse(true);
        animations.add(rotation);
    }

    private void addStand(double x) {
        var plate = new Cylinder(plateRadius, plateThickness);
        plate.setMaterial(plateMaterial);
        plate.setTranslateX(x);
        plate.setTranslateZ(-pillarHeight - plateThickness);
        plate.setRotationAxis(Rotate.X_AXIS);
        plate.setRotate(90);
        plate.drawModeProperty().bind(PacManGames3dUI.PY_3D_DRAW_MODE);

        var pillar = new Cylinder(1, pillarHeight);
        pillar.setMaterial(pillarMaterial);
        pillar.setTranslateX(x);
        pillar.setTranslateZ(-0.5 * pillarHeight);
        pillar.setRotationAxis(Rotate.X_AXIS);
        pillar.setRotate(90);
        pillar.drawModeProperty().bind(PacManGames3dUI.PY_3D_DRAW_MODE);

        standsGroup.getChildren().addAll(plate, pillar);
    }

    public int maxLives() {
        return maxLives;
    }

    public void update(int numLives) {
        for (int i = 0; i < maxLives; ++i) {
            pac3DList.get(i).setVisible(i < numLives);
        }
    }

    public void startAnimation() {
        animations.forEach(Animation::play);
    }

    public void stopAnimation() {
        animations.forEach(Animation::stop);
    }
}