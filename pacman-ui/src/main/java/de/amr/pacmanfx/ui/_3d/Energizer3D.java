/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.uilib.animation.AnimationManager;
import javafx.animation.*;
import javafx.animation.Animation.Status;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;

import static de.amr.pacmanfx.Validations.requireNonNegative;
import static java.util.Objects.requireNonNull;

/**
 * 3D energizer pellet.
 */
public class Energizer3D implements Eatable3D {

    private final Sphere sphere;
    private final ScaleTransition pumpingAnimation;
    private final Animation hideAfterSmallDelay;
    private final AnimationManager animationMgr;
    private Animation hideAndEatAnimation;

    public Energizer3D(double radius, AnimationManager animationMgr) {
        this.animationMgr = requireNonNull(animationMgr);
        requireNonNegative(radius, "Energizer radius must be positive but is %f");
        sphere = new Sphere(radius);
        // 3 full blinks per second
        pumpingAnimation = new ScaleTransition(Duration.millis(166.6), sphere);
        pumpingAnimation.setAutoReverse(true);
        pumpingAnimation.setCycleCount(Animation.INDEFINITE);
        pumpingAnimation.setInterpolator(Interpolator.EASE_BOTH);
        pumpingAnimation.setFromX(Settings3D.ENERGIZER_3D_MAX_SCALING);
        pumpingAnimation.setFromY(Settings3D.ENERGIZER_3D_MAX_SCALING);
        pumpingAnimation.setFromZ(Settings3D.ENERGIZER_3D_MAX_SCALING);
        pumpingAnimation.setToX(Settings3D.ENERGIZER_3D_MIN_SCALING);
        pumpingAnimation.setToY(Settings3D.ENERGIZER_3D_MIN_SCALING);
        pumpingAnimation.setToZ(Settings3D.ENERGIZER_3D_MIN_SCALING);

        hideAfterSmallDelay = new PauseTransition(Duration.seconds(0.05));
        hideAfterSmallDelay.setOnFinished(e -> shape3D().setVisible(false));
    }

    public void startPumping() {
        animationMgr.registerAndPlayFromStart(sphere, "Energizer_Pumping", pumpingAnimation);
    }

    public void setEatenAnimation(Animation eatenAnimation) {
        hideAndEatAnimation = new SequentialTransition(hideAfterSmallDelay, eatenAnimation);
    }

    @Override
    public void onEaten() {
        pumpingAnimation.stop();
        if (hideAndEatAnimation != null) {
            animationMgr.registerAndPlayFromStart(sphere, "Energizer_HideAndEat", hideAndEatAnimation);
        } else {
            animationMgr.registerAndPlayFromStart(sphere, "Energizer_Hide", hideAfterSmallDelay);
        }
    }

    @Override
    public String toString() {
        String pumpingText = pumpingAnimation.getStatus() == Status.RUNNING ? " (pumping)" : "";
        return String.format("[Energizer%s, tile: %s]", pumpingText, tile());
    }

    @Override
    public Shape3D shape3D() {
        return sphere;
    }
}