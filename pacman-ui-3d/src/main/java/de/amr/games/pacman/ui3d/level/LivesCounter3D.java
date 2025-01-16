/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.ui2d.lib.Ufx;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.beans.property.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui3d.GlobalProperties3d.PY_3D_DRAW_MODE;

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

    private final Node[] pacShapes;
    private final PointLight light = new PointLight();

    public LivesCounter3D(Node[] pacShapes, int shapeHeight) {
        this.pacShapes = pacShapes;

        pillarMaterialPy.bind(pillarColorPy.map(Ufx::coloredMaterial));
        plateMaterialPy.bind((plateColorPy.map(Ufx::coloredMaterial)));

        light.setMaxRange  (TS * (pacShapes.length + 1));
        light.setTranslateX(TS * (pacShapes.length - 1));
        light.setTranslateY(TS * (-1));
        light.translateZProperty().bind(pillarHeightPy.add(20).multiply(-1));

        var standsGroup = new Group();
        for (int i = 0; i < pacShapes.length; ++i) {
            int x = i * 2 * TS;
            standsGroup.getChildren().add(createStand(x));
            Node shape = pacShapes[i];
            shape.setUserData(i);
            shape.setTranslateX(x);
            shape.setTranslateY(0);
            shape.translateZProperty().bind(pillarHeightPy.add(plateThicknessPy).add(0.5 * shapeHeight).negate());
            shape.visibleProperty().bind(livesCountPy.map(count -> count.intValue() > (int) shape.getUserData()));
            getChildren().add(shape);
        }
        resetShapes();
        getChildren().addAll(standsGroup, light);
    }

    public PointLight light() {
        return light;
    }

    public void resetShapes() {
        for (Node shape : pacShapes) {
            shape.setRotationAxis(Rotate.Z_AXIS);
            shape.setRotate(240);
        }
    }

    public Animation createAnimation() {
        var transition = new ParallelTransition();
        for (Node shape : pacShapes) {
            var rotation = new RotateTransition(Duration.seconds(10.0), shape);
            rotation.setAxis(Rotate.Z_AXIS);
            rotation.setFromAngle(240);
            rotation.setToAngle(300);
            rotation.setInterpolator(Interpolator.LINEAR);
            rotation.setCycleCount(Animation.INDEFINITE);
            rotation.setAutoReverse(true);
            transition.getChildren().add(rotation);
        }
        return transition;
    }

    private Group createStand(double x) {
        var pillar = new Cylinder(1, 0.1);
        pillar.heightProperty().bind(pillarHeightPy);
        pillar.materialProperty().bind(pillarMaterialPy);
        pillar.setTranslateX(x);
        pillar.translateZProperty().bind(pillarHeightPy.multiply(-0.5));
        pillar.setRotationAxis(Rotate.X_AXIS);
        pillar.setRotate(90);
        pillar.drawModeProperty().bind(PY_3D_DRAW_MODE);

        var podium = new Cylinder();
        podium.radiusProperty().bind(plateRadiusPy);
        podium.heightProperty().bind(plateThicknessPy);
        podium.materialProperty().bind(plateMaterialPy);
        podium.setTranslateX(x);
        podium.translateZProperty().bind(pillarHeightPy.add(plateThicknessPy).negate());
        podium.setRotationAxis(Rotate.X_AXIS);
        podium.setRotate(90);
        podium.drawModeProperty().bind(PY_3D_DRAW_MODE);

        return new Group(pillar, podium);
    }
}