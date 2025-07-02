/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

/**
 * 3D ghost house door.
 */
public class Door3D extends Group {

    private static final int NUM_VERTICAL_BARS = 2;

    private final DoubleProperty barThicknessPy = new SimpleDoubleProperty(0.75);

    private PhongMaterial barMaterial;
    private ManagedAnimation openCloseAnimation;
    private Group leftWing;
    private Group rightWing;

    public Door3D(
        AnimationManager animationManager,
        Vector2i leftWingTile,
        Vector2i rightWingTile,
        Color color,
        double height)
    {
        requireNonNull(animationManager);
        requireNonNull(leftWingTile);
        requireNonNull(color);

        barMaterial = Ufx.coloredPhongMaterial(color);
        leftWing = createDoorWing(leftWingTile, height);
        rightWing = createDoorWing(rightWingTile, height);
        getChildren().addAll(leftWing, rightWing);

        openCloseAnimation = new ManagedAnimation(animationManager, "Door_OpenClose") {
            @Override
            protected Animation createAnimation() {
                return new Timeline(
                    new KeyFrame(Duration.seconds(0.75), new KeyValue(barThicknessPy, 0)),
                    new KeyFrame(Duration.seconds(1.5),  new KeyValue(barThicknessPy, 0.75))
                );
            }
        };
    }

    private Group createDoorWing(Vector2i tile, double height) {
        var root = new Group();
        root.setTranslateX(tile.x() * TS);
        root.setTranslateY(tile.y() * TS);

        for (int i = 0; i < NUM_VERTICAL_BARS; ++i) {
            var verticalBar = new Cylinder(barThicknessPy.get(), height);
            verticalBar.radiusProperty().bind(barThicknessPy);
            verticalBar.setMaterial(barMaterial);
            verticalBar.setTranslateX(i * 4 + 2);
            verticalBar.setTranslateY(4);
            verticalBar.translateZProperty().bind(verticalBar.heightProperty().multiply(-0.5));
            verticalBar.setRotationAxis(Rotate.X_AXIS);
            verticalBar.setRotate(90);
            root.getChildren().add(verticalBar);
        }

        var horizontalBar = new Cylinder(barThicknessPy.get(), 14);
        horizontalBar.radiusProperty().bind(barThicknessPy);
        horizontalBar.setMaterial(barMaterial);
        horizontalBar.setTranslateX(4);
        horizontalBar.setTranslateY(4);
        horizontalBar.setTranslateZ(0.25 - horizontalBar.getHeight() * 0.5);
        horizontalBar.setRotationAxis(Rotate.Z_AXIS);
        horizontalBar.setRotate(90);
        root.getChildren().add(horizontalBar);

        return root;
    }

    public void destroy() {
        openCloseAnimation.destroy();
        openCloseAnimation = null;
        destroyDoorWing(leftWing);
        leftWing = null;
        destroyDoorWing(rightWing);
        rightWing = null;
        getChildren().clear();
        barMaterial = null;
    }

    private void destroyDoorWing(Group doorWing) {
        for (Node node : doorWing.getChildren()) {
            if (node instanceof Cylinder bar) {
                bar.radiusProperty().unbind();
                bar.translateZProperty().unbind();
                bar.setMaterial(null);
            }
        }
    }

    public ManagedAnimation openCloseAnimation() {
        return openCloseAnimation;
    }

}