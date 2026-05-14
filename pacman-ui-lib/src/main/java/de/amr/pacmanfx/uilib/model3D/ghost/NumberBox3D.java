/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.ghost;

import de.amr.pacmanfx.model.GameLevelEntity;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.animation.ManagedAnimationsRegistry;
import javafx.animation.*;
import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class NumberBox3D extends Box implements GameLevelEntity {

    public enum AnimationID { ROTATING_NUMBER }

    public static final int DEFAULT_SIZE_X = 14;
    public static final int DEFAULT_SIZE_Y = 8;
    public static final int DEFAULT_SIZE_Z = 8;

    private final ManagedAnimation animation;

    public NumberBox3D(ManagedAnimationsRegistry animations, Image numberImage) {
        super(DEFAULT_SIZE_X, DEFAULT_SIZE_Y, DEFAULT_SIZE_Z);

        final PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(numberImage);
        setMaterial(material);

        animation = new ManagedAnimation("numberAnimation");
        animation.setFactory(() -> {
            final Duration riseDuration = Duration.seconds(3);

            final var rotate = new RotateTransition(Duration.seconds(0.5), this);
            rotate.setAxis(Rotate.X_AXIS);
            rotate.setFromAngle(0);
            rotate.setToAngle(360);
            rotate.setCycleCount(4);
            rotate.setInterpolator(Interpolator.LINEAR);

            final var shrink = new ScaleTransition(riseDuration, this);
            shrink.setToX(0);
            shrink.setToY(0);
            shrink.setToZ(0);
            shrink.setInterpolator(Interpolator.EASE_IN);

            final var rise = new TranslateTransition(riseDuration, this);
            rise.setFromZ(getTranslateZ());
            rise.setToZ(getTranslateZ() - 50);
            rise.setByX(0);
            rise.setByY(0);
            rise.setInterpolator(Interpolator.EASE_IN);

            return new SequentialTransition(
                new PauseTransition(Duration.seconds(0.5)),
                new ParallelTransition(rotate /*, shrink, rise*/)
            );
        });
        animations.register(AnimationID.ROTATING_NUMBER, animation);
    }

    public ManagedAnimation animation() {
        return animation;
    }
}
