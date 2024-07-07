/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.entity;

import de.amr.games.pacman.ui2d.util.Ufx;
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
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static de.amr.games.pacman.lib.Globals.TS;

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

    public final IntegerProperty livesShownPy = new SimpleIntegerProperty(this, "livesShown", 0) {
        @Override
        protected void invalidated() {
            int numLives = get();
            for (int i = 0; i < maxLives; ++i) {
                pacShapes.get(i).setVisible(i < numLives);
            }
            Logger.info("Lives counter updated, visible lives {}", numLives);
        }
    };

    public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

    private final ObjectProperty<PhongMaterial> pillarMaterialPy = new SimpleObjectProperty<>(this, "pillarMaterial", new PhongMaterial());
    private final ObjectProperty<PhongMaterial> plateMaterialPy = new SimpleObjectProperty<>(this, "plateMaterial", new PhongMaterial());

    private final Group standsGroup = new Group();
    private final PointLight light = new PointLight();

    private final List<Pac3D> pacShapes = new ArrayList<>();
    private final List<Animation> animations = new ArrayList<>();

    private final int maxLives;

    public LivesCounter3D(int maxLives, Supplier<Pac3D> shapeFactory)
    {
        this.maxLives = maxLives;

        pillarMaterialPy.bind(Bindings.createObjectBinding(() -> Ufx.coloredMaterial(pillarColorPy.get()), pillarColorPy));
        plateMaterialPy.bind(Bindings.createObjectBinding(() -> Ufx.coloredMaterial(plateColorPy.get()), plateColorPy));

        light.setMaxRange  (TS * (maxLives + 1));
        light.setTranslateX(TS * (maxLives - 1));
        light.setTranslateY(TS * (-1));
        light.translateZProperty().bind(pillarHeightPy.add(20).multiply(-1));

        getChildren().addAll(standsGroup, light);

        for (int i = 0; i < maxLives; ++i) {
            addItem(shapeFactory.get(), true);
        }
    }

    public PointLight light() {
        return light;
    }

    private void addItem(Pac3D pac3D, boolean lookRight) {
        int x = pacShapes.size() * 2 * TS;
        addStand(x);
        double pacRadius = pac3D.getBoundsInLocal().getHeight() * 0.5;
        pac3D.position.setX(x);
        pac3D.position.zProperty().bind(Bindings.createDoubleBinding(
            () -> -(pillarHeightPy.get() + plateThicknessPy.get() + pacRadius),
            pillarHeightPy, plateThicknessPy)
        );
        if (lookRight) {
            pac3D.setRotationAxis(Rotate.Z_AXIS);
            pac3D.setRotate(180);
        }
        pac3D.drawModePy.bind(drawModePy);
        pacShapes.add(pac3D);
        getChildren().add(pac3D);

        var rotation = new RotateTransition(Duration.seconds(10.0), pac3D);
        rotation.setAxis(Rotate.Z_AXIS);
        rotation.setByAngle(180);
        rotation.setInterpolator(Interpolator.LINEAR);
        rotation.setCycleCount(Animation.INDEFINITE);
        rotation.setAutoReverse(true);
        animations.add(rotation);
    }

    private void addStand(double x) {
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

        var pillar = new Cylinder(1, 0.1);
        pillar.heightProperty().bind(pillarHeightPy);
        pillar.materialProperty().bind(pillarMaterialPy);
        pillar.setTranslateX(x);
        pillar.translateZProperty().bind(pillarHeightPy.multiply(-0.5));
        pillar.setRotationAxis(Rotate.X_AXIS);
        pillar.setRotate(90);
        pillar.drawModeProperty().bind(PacManGames3dUI.PY_3D_DRAW_MODE);

        standsGroup.getChildren().addAll(plate, pillar);
    }

    public int maxLives() {
        return maxLives;
    }

    public void startAnimation() {
        animations.forEach(Animation::play);
    }

    public void stopAnimation() {
        animations.forEach(Animation::stop);
    }
}