/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.ui2d.util.Ufx;
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
import static de.amr.games.pacman.ui3d.PacManGames3dApp.PY_3D_DRAW_MODE;

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
    private final ParallelTransition shapesRotation = new ParallelTransition();

    public LivesCounter3D(Node[] shapes, int shapeHeight) {
        pillarMaterialPy.bind(pillarColorPy.map(Ufx::coloredMaterial));
        plateMaterialPy.bind((plateColorPy.map(Ufx::coloredMaterial)));

        light.setMaxRange  (TS * (shapes.length + 1));
        light.setTranslateX(TS * (shapes.length - 1));
        light.setTranslateY(TS * (-1));
        light.translateZProperty().bind(pillarHeightPy.add(20).multiply(-1));

        var standsGroup = new Group();
        for (int i = 0; i < shapes.length; ++i) {
            int x = i * 2 * TS;
            standsGroup.getChildren().add(createStand(x));

            Node shape = shapes[i];
            shape.setUserData(i);
            shape.setTranslateX(x);
            shape.setTranslateY(0);
            shape.translateZProperty().bind(pillarHeightPy.add(plateThicknessPy).add(0.5 * shapeHeight).negate());
            shape.setRotationAxis(Rotate.Z_AXIS);
            shape.setRotate(180);
            shape.visibleProperty().bind(livesCountPy.map(count -> count.intValue() > (int) shape.getUserData()));
            getChildren().add(shape);

            var rotation = new RotateTransition(Duration.seconds(10.0), shape);
            rotation.setAxis(Rotate.Z_AXIS);
            rotation.setByAngle(180);
            rotation.setInterpolator(Interpolator.LINEAR);
            rotation.setCycleCount(Animation.INDEFINITE);
            rotation.setAutoReverse(true);
            shapesRotation.getChildren().add(rotation);
        }
        getChildren().addAll(standsGroup, light);
    }

    public PointLight light() {
        return light;
    }

    public Animation shapesRotation() {
        return shapesRotation;
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