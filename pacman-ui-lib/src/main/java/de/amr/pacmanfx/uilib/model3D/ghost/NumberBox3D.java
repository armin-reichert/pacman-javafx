/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.ghost;

import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.animation.ManagedAnimationsRegistry;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class NumberBox3D extends Box {

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
            var rt = new RotateTransition(Duration.seconds(1), this);
            rt.setAxis(Rotate.X_AXIS);
            rt.setFromAngle(0);
            rt.setToAngle(360);
            rt.setInterpolator(Interpolator.LINEAR);
            rt.setRate(0.75);
            return rt;
        });
        animations.register(AnimationID.ROTATING_NUMBER, animation);
    }

    public ManagedAnimation animation() {
        return animation;
    }
}
