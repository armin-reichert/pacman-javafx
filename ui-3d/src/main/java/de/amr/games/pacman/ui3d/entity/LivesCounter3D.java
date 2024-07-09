/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.entity;

import de.amr.games.pacman.ui3d.PacManGames3dUI;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
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
import java.util.function.Supplier;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui2d.util.Ufx.coloredMaterial;

/**
 * Displays a Pac-Man shape for each live remaining.
 *
 * @author Armin Reichert
 */
public class LivesCounter3D extends Group {

    public final DoubleProperty pillarHeightPy = new SimpleDoubleProperty(this, "pillarHeight", 8);
    public final ObjectProperty<Color> pillarColorPy = new SimpleObjectProperty<>(this, "pillarColor", Color.grayRgb(120));
    public final DoubleProperty plateRadiusPy = new SimpleDoubleProperty(this, "plateRadius", 6);
    public final DoubleProperty plateThicknessPy = new SimpleDoubleProperty(this, "plateThickness", 1);
    public final ObjectProperty<Color> plateColorPy = new SimpleObjectProperty<>(this, "plateColor", Color.grayRgb(180));
    public final IntegerProperty livesCountPy = new SimpleIntegerProperty(this, "livesCount", 0);
    public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

    private final ObjectProperty<PhongMaterial> pillarMaterialPy = new SimpleObjectProperty<>(this, "pillarMaterial", new PhongMaterial());
    private final ObjectProperty<PhongMaterial> plateMaterialPy = new SimpleObjectProperty<>(this, "plateMaterial", new PhongMaterial());

    private final PointLight light = new PointLight();
    private final List<Animation> animations = new ArrayList<>();

    public LivesCounter3D(int maxLives, Supplier<Pac3D> shapeFactory)
    {
        pillarMaterialPy.bind(Bindings.createObjectBinding(() -> coloredMaterial(pillarColorPy.get()), pillarColorPy));
        plateMaterialPy.bind(Bindings.createObjectBinding(() -> coloredMaterial(plateColorPy.get()), plateColorPy));

        light.setMaxRange  (TS * (maxLives + 1));
        light.setTranslateX(TS * (maxLives - 1));
        light.setTranslateY(TS * (-1));
        light.translateZProperty().bind(pillarHeightPy.add(20).multiply(-1));

        var standsGroup = new Group();
        for (int i = 0; i < maxLives; ++i) {
            int x = i * 2 * TS;

            standsGroup.getChildren().add(createStand(x));

            Pac3D pac3D = shapeFactory.get();
            pac3D.setUserData(i);
            pac3D.position.setX(x);
            pac3D.position.setY(0);
            double pacRadius = pac3D.getBoundsInLocal().getHeight() * 0.5;
            pac3D.position.zProperty().bind(Bindings.createDoubleBinding(
                () -> -(pillarHeightPy.get() + plateThicknessPy.get() + pacRadius),
                pillarHeightPy, plateThicknessPy)
            );
            pac3D.setRotationAxis(Rotate.Z_AXIS);
            pac3D.setRotate(180);
            pac3D.drawModePy.bind(drawModePy);
            pac3D.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> (int) pac3D.getUserData() < livesCountPy.get(), livesCountPy));
            getChildren().add(pac3D);

            var rotation = new RotateTransition(Duration.seconds(10.0), pac3D);
            rotation.setAxis(Rotate.Z_AXIS);
            rotation.setByAngle(180);
            rotation.setInterpolator(Interpolator.LINEAR);
            rotation.setCycleCount(Animation.INDEFINITE);
            rotation.setAutoReverse(true);
            animations.add(rotation);
        }
        getChildren().addAll(standsGroup, light);
    }

    public PointLight light() {
        return light;
    }

    private Group createStand(double x) {
        var pillar = new Cylinder(1, 0.1);
        pillar.heightProperty().bind(pillarHeightPy);
        pillar.materialProperty().bind(pillarMaterialPy);
        pillar.setTranslateX(x);
        pillar.translateZProperty().bind(pillarHeightPy.multiply(-0.5));
        pillar.setRotationAxis(Rotate.X_AXIS);
        pillar.setRotate(90);
        pillar.drawModeProperty().bind(PacManGames3dUI.PY_3D_DRAW_MODE);

        var plate = new Cylinder();
        plate.radiusProperty().bind(plateRadiusPy);
        plate.heightProperty().bind(plateThicknessPy);
        plate.materialProperty().bind(plateMaterialPy);
        plate.setTranslateX(x);
        plate.translateZProperty().bind(Bindings.createDoubleBinding(
            () -> -pillarHeightPy.get() - plateThicknessPy.get(),
            pillarHeightPy, plateThicknessPy));
        plate.setRotationAxis(Rotate.X_AXIS);
        plate.setRotate(90);
        plate.drawModeProperty().bind(PacManGames3dUI.PY_3D_DRAW_MODE);

        return new Group(pillar, plate);
    }

    public void startAnimation() {
        animations.forEach(Animation::play);
    }

    public void stopAnimation() {
        animations.forEach(Animation::stop);
    }
}