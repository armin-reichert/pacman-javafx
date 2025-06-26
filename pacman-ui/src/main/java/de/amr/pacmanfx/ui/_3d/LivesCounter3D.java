/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
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

import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

/**
 * Displays a Pac-Man shape for each live remaining.
 */
public class LivesCounter3D extends Group {

    private final DoubleProperty pillarHeightPy = new SimpleDoubleProperty(8);
    private final ObjectProperty<Color> pillarColorPy = new SimpleObjectProperty<>(Color.grayRgb(120));
    private final DoubleProperty plateRadiusPy = new SimpleDoubleProperty(6);
    private final DoubleProperty plateThicknessPy = new SimpleDoubleProperty(1);
    private final ObjectProperty<Color> plateColorPy = new SimpleObjectProperty<>(Color.grayRgb(180));
    private final IntegerProperty livesCountPy = new SimpleIntegerProperty(0);
    private final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(DrawMode.FILL);
    private final ObjectProperty<PhongMaterial> pillarMaterialPy = new SimpleObjectProperty<>(new PhongMaterial());
    private final ObjectProperty<PhongMaterial> plateMaterialPy = new SimpleObjectProperty<>(new PhongMaterial());

    private final Node[] pacShapes;
    private final PointLight light = new PointLight();

    private final AnimationManager animationManager;
    private Animation lookingAroundAnimation;

    public LivesCounter3D(AnimationManager animationManager, Node[] pacShapes) {
        this.animationManager = requireNonNull(animationManager);
        this.pacShapes = pacShapes;

        pillarMaterialPy.bind(pillarColorPy.map(Ufx::coloredPhongMaterial));
        plateMaterialPy.bind((plateColorPy.map(Ufx::coloredPhongMaterial)));

        light.setMaxRange  (TS * (pacShapes.length + 1));
        light.setTranslateX(TS * (pacShapes.length - 1));
        light.setTranslateY(TS * (-1));
        light.translateZProperty().bind(pillarHeightPy.add(20).multiply(-1));

        var standsGroup = new Group();
        for (int i = 0; i < pacShapes.length; ++i) {
            final Node pacShape = pacShapes[i];
            final int x = i * 2 * TS;
            final double shapeRadius = 0.5 * pacShape.getBoundsInParent().getHeight(); // take scale transform into account!

            pacShape.setUserData(i);
            pacShape.setTranslateX(x);
            pacShape.setTranslateY(0);
            // let Pac shape sit on top of plate
            pacShape.translateZProperty().bind(pillarHeightPy.add(plateThicknessPy).add(shapeRadius).negate());
            pacShape.visibleProperty().bind(livesCountPy.map(count -> count.intValue() > (int) pacShape.getUserData()));

            standsGroup.getChildren().add(createStand(x));
            getChildren().add(pacShape);
        }
        setInitialShapeRotation();
        getChildren().addAll(standsGroup, light);
    }

    public ObjectProperty<DrawMode> drawModeProperty() { return drawModePy; }

    public IntegerProperty livesCountProperty() { return livesCountPy; }

    public ObjectProperty<Color> pillarColorProperty() { return pillarColorPy; }

    public ObjectProperty<Color> plateColorProperty() { return plateColorPy; }

    public PointLight light() {
        return light;
    }

    public void playLookingAroundAnimation() {
        if (lookingAroundAnimation == null) {
            lookingAroundAnimation = createPacShapesLookAroundAnimation();
            animationManager.register("LivesCounter_LookingAroundAnimation", lookingAroundAnimation);
        }
        lookingAroundAnimation.play();
    }

    private Animation createPacShapesLookAroundAnimation() {
        var animation = new ParallelTransition();
        for (Node pacShape : pacShapes) {
            var rotation = new RotateTransition(Duration.seconds(10.0), pacShape);
            rotation.setAxis(Rotate.Z_AXIS);
            rotation.setFromAngle(240);
            rotation.setToAngle(300);
            rotation.setInterpolator(Interpolator.LINEAR);
            rotation.setCycleCount(Animation.INDEFINITE);
            rotation.setAutoReverse(true);
            animation.getChildren().add(rotation);
        }
        setInitialShapeRotation();
        return animation;
    }

    private void setInitialShapeRotation() {
        for (Node shape : pacShapes) {
            shape.setRotationAxis(Rotate.Z_AXIS);
            shape.setRotate(240);
        }
    }

    private Group createStand(double x) {
        var pillar = new Cylinder(1, 0.1);
        pillar.heightProperty().bind(pillarHeightPy);
        pillar.materialProperty().bind(pillarMaterialPy);
        pillar.setTranslateX(x);
        pillar.translateZProperty().bind(pillarHeightPy.multiply(-0.5));
        pillar.setRotationAxis(Rotate.X_AXIS);
        pillar.setRotate(90);
        pillar.drawModeProperty().bind(drawModePy);

        var podium = new Cylinder();
        podium.radiusProperty().bind(plateRadiusPy);
        podium.heightProperty().bind(plateThicknessPy);
        podium.materialProperty().bind(plateMaterialPy);
        podium.setTranslateX(x);
        podium.translateZProperty().bind(pillarHeightPy.add(plateThicknessPy).negate());
        podium.setRotationAxis(Rotate.X_AXIS);
        podium.setRotate(90);
        podium.drawModeProperty().bind(drawModePy);

        return new Group(pillar, podium);
    }
}