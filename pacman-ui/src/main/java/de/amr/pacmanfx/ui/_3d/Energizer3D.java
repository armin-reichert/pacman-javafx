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

import static de.amr.pacmanfx.Validations.requireNonNegative;

/**
 * 3D energizer pellet.
 *
 * @author Armin Reichert
 */
public class Energizer3D extends Sphere implements Eatable3D {

    private static final double MIN_SCALING = 0.20;
    private static final double MAX_SCALING = 1.00;

    private final ScaleTransition pumpingAnimation;
    private final Animation hideAfterSmallDelay;
    private Animation eatenAnimation;

    public Energizer3D(double radius) {
        requireNonNegative(radius, "Energizer radius must be positive but is %f");
        setRadius(radius);
        // 3 full blinks per second
        pumpingAnimation = new ScaleTransition(Duration.millis(166.6), this);
        pumpingAnimation.setAutoReverse(true);
        pumpingAnimation.setCycleCount(Animation.INDEFINITE);
        pumpingAnimation.setInterpolator(Interpolator.EASE_BOTH);
        pumpingAnimation.setFromX(MAX_SCALING);
        pumpingAnimation.setFromY(MAX_SCALING);
        pumpingAnimation.setFromZ(MAX_SCALING);
        pumpingAnimation.setToX(MIN_SCALING);
        pumpingAnimation.setToY(MIN_SCALING);
        pumpingAnimation.setToZ(MIN_SCALING);

        hideAfterSmallDelay = new PauseTransition(Duration.seconds(0.05));
        hideAfterSmallDelay.setOnFinished(e -> shape3D().setVisible(false));
    }

    public void startPumping() {
        pumpingAnimation.playFromStart();
    }

    public void stopPumping() {
        pumpingAnimation.stop();
    }

    public void setEatenAnimation(Animation animation) {
        this.eatenAnimation = animation;
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
        var pumping = pumpingAnimation.getStatus() == Status.RUNNING ? ", pumping" : "";
        return String.format("[Energizer%s, tile: %s]", pumping, tile());
    }

    @Override
    public Shape3D shape3D() {
        return this;
    }
}