/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.world;

import de.amr.pacmanfx.model.GameLevelEntity;
import de.amr.pacmanfx.uilib.model3D.animation.MaterialColorAnimation3D;
import javafx.animation.*;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class NumberBox3D extends Group implements GameLevelEntity {

    public enum AnimationID { ROTATING_NUMBER }

    public static final int DEFAULT_SIZE_X = 14;
    public static final int DEFAULT_SIZE_Y = 8;
    public static final int DEFAULT_SIZE_Z = 8;

    private final Group riseGroup = new Group();
    private final Group rotateGroup = new Group();
    private final Box box;

    public NumberBox3D(Image numberImage) {

        box = new Box(DEFAULT_SIZE_X, DEFAULT_SIZE_Y, DEFAULT_SIZE_Z);

        final PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(numberImage);
        box.setMaterial(material);

        // Build transform hierarchy
        rotateGroup.getChildren().add(box);
        riseGroup.getChildren().add(rotateGroup);
        getChildren().add(riseGroup);
    }

    public Animation createAnimation() {
        final Duration riseDuration = Duration.seconds(1);

        // rotate only rotateGroup
        final var rotate = new RotateTransition(Duration.seconds(0.5), rotateGroup);
        rotate.setAxis(Rotate.X_AXIS);
        rotate.setFromAngle(0);
        rotate.setToAngle(360);
        rotate.setCycleCount(5);
        rotate.setInterpolator(Interpolator.LINEAR);

        // translate only riseGroup (world-space Z)
        final var rise = new TranslateTransition(riseDuration, riseGroup);
        rise.setFromZ(0);
        rise.setToZ(-40);
        rise.setInterpolator(Interpolator.EASE_IN);

        // fade material
        PhongMaterial material = (PhongMaterial) box.getMaterial();
        Animation fade = MaterialColorAnimation3D.create(1, material, material.getDiffuseColor(), Color.TRANSPARENT);

        return new SequentialTransition(
            new PauseTransition(Duration.seconds(0.5)),
            new ParallelTransition(rotate, rise),
            fade
        );
    }

    public Point3D riseGroupPosition() {
        return new Point3D(
            riseGroup.getTranslateX(),
            riseGroup.getTranslateY(),
            riseGroup.getTranslateZ()
        );
    }
}
