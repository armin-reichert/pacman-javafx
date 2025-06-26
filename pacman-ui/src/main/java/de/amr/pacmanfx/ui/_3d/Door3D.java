/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

/**
 * 3D ghost house door.
 */
public class Door3D extends Group {

    private static final int NUM_VERTICAL_BARS = 2;

    private final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(DrawMode.FILL);
    private final DoubleProperty barThicknessPy = new SimpleDoubleProperty(0.75);
    private final PhongMaterial barMaterial;

    private final AnimationManager animationManager;
    private Animation openCloseAnimation;

    public Door3D(AnimationManager animationManager, Vector2i leftWingTile, Vector2i rightWingTile, Color color, double height) {
        this.animationManager = requireNonNull(animationManager);
        barMaterial = Ufx.coloredPhongMaterial(color);
        getChildren().addAll(createDoorWing(leftWingTile, height), createDoorWing(rightWingTile, height));
    }

    private Group createDoorWing(Vector2i tile, double height) {
        var group = new Group();

        group.setTranslateX(tile.x() * TS);
        group.setTranslateY(tile.y() * TS);

        for (int i = 0; i < NUM_VERTICAL_BARS; ++i) {
            var verticalBar = new Cylinder(barThicknessPy.get(), height);
            verticalBar.radiusProperty().bind(barThicknessPy);
            verticalBar.setMaterial(barMaterial);
            verticalBar.setTranslateX(i * 4 + 2);
            verticalBar.setTranslateY(4);
            verticalBar.translateZProperty().bind(verticalBar.heightProperty().multiply(-0.5));
            verticalBar.setRotationAxis(Rotate.X_AXIS);
            verticalBar.setRotate(90);
            verticalBar.drawModeProperty().bind(drawModePy);
            group.getChildren().add(verticalBar);
        }

        var horizontalBar = new Cylinder(barThicknessPy.get(), 14);
        horizontalBar.radiusProperty().bind(barThicknessPy);
        horizontalBar.setMaterial(barMaterial);
        horizontalBar.setTranslateX(4);
        horizontalBar.setTranslateY(4);
        horizontalBar.setTranslateZ(0.25 - horizontalBar.getHeight() * 0.5);
        horizontalBar.setRotationAxis(Rotate.Z_AXIS);
        horizontalBar.setRotate(90);
        horizontalBar.drawModeProperty().bind(drawModePy);
        group.getChildren().add(horizontalBar);

        return group;
    }

    public ObjectProperty<DrawMode> drawModeProperty() { return drawModePy; }

    private Animation createOpenCloseAnimation() {
        return new Timeline(
            new KeyFrame(Duration.seconds(0.75), new KeyValue(barThicknessPy, 0)),
            new KeyFrame(Duration.seconds(1.5),  new KeyValue(barThicknessPy, 0.75))
        );
    }

    public void playOpenCloseAnimation() {
        if (openCloseAnimation == null) {
            openCloseAnimation = createOpenCloseAnimation();
            animationManager.register("Door_OpenClose", openCloseAnimation);
        }
        openCloseAnimation.playFromStart();
    }
}