/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import javafx.animation.*;
import javafx.animation.Animation.Status;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.Validations.requireNonNegative;
import static java.util.Objects.requireNonNull;

/**
 * 3D energizer pellet.
 */
public class Energizer3D implements Eatable3D {

    private static final double MIN_SCALING = 0.20;
    private static final double MAX_SCALING = 1.00;

    private final Sphere sphere;
    private final ScaleTransition pumpingAnimation;
    private final Animation hideAfterSmallDelay;
    private Animation eatenAnimation;

    private final List<Animation> animations = new ArrayList<>();

    public Energizer3D(double radius) {
        requireNonNegative(radius, "Energizer radius must be positive but is %f");
        sphere = new Sphere(radius);
        // 3 full blinks per second
        pumpingAnimation = new ScaleTransition(Duration.millis(166.6), sphere);
        pumpingAnimation.setAutoReverse(true);
        pumpingAnimation.setCycleCount(Animation.INDEFINITE);
        pumpingAnimation.setInterpolator(Interpolator.EASE_BOTH);
        pumpingAnimation.setFromX(MAX_SCALING);
        pumpingAnimation.setFromY(MAX_SCALING);
        pumpingAnimation.setFromZ(MAX_SCALING);
        pumpingAnimation.setToX(MIN_SCALING);
        pumpingAnimation.setToY(MIN_SCALING);
        pumpingAnimation.setToZ(MIN_SCALING);
        animations.add(pumpingAnimation);

        hideAfterSmallDelay = new PauseTransition(Duration.seconds(0.05));
        hideAfterSmallDelay.setOnFinished(e -> shape3D().setVisible(false));
        animations.add(hideAfterSmallDelay);
    }

    public void startPumping() {
        pumpingAnimation.playFromStart();
    }

    public void stopAnimations() {
        animations.forEach(animation -> {
            try {
                animation.stop();
            } catch (IllegalStateException x) {
                Logger.warn("Animation could not be stopped (probably embedded in another one)");
            }
        });
    }

    public void setEatenAnimation(Animation animation) {
        this.eatenAnimation = requireNonNull(animation);
    }

    @Override
    public void onEaten() {
        pumpingAnimation.stop();
        if (eatenAnimation != null) {
            new SequentialTransition(hideAfterSmallDelay, eatenAnimation).play();
        } else {
            hideAfterSmallDelay.play();
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