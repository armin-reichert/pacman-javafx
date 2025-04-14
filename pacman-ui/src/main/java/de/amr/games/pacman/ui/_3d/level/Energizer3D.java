/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._3d.level;

import javafx.animation.*;
import javafx.animation.Animation.Status;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;

import static de.amr.games.pacman.Globals.assertNonNegative;

/**
 * 3D energizer pellet.
 *
 * @author Armin Reichert
 */
public class Energizer3D extends Sphere implements Eatable3D {

    private static final double MIN_SCALE = 0.25;

    private final ScaleTransition pumpingAnimation;
    private final Animation hideAfterSmallDelay;
    private Animation eatenAnimation;

    public Energizer3D(double radius) {
        assertNonNegative(radius, "Energizer radius must be positive but is %f");
        setRadius(radius);
        setUserData(this);

        pumpingAnimation = new ScaleTransition(Duration.seconds(1.0 / 4), this);
        pumpingAnimation.setAutoReverse(true);
        pumpingAnimation.setCycleCount(Animation.INDEFINITE);
        pumpingAnimation.setInterpolator(Interpolator.EASE_BOTH);
        pumpingAnimation.setFromX(1.0);
        pumpingAnimation.setFromY(1.0);
        pumpingAnimation.setFromZ(1.0);
        pumpingAnimation.setToX(MIN_SCALE);
        pumpingAnimation.setToY(MIN_SCALE);
        pumpingAnimation.setToZ(MIN_SCALE);

        hideAfterSmallDelay = new PauseTransition(Duration.seconds(0.05));
        hideAfterSmallDelay.setOnFinished(e -> setVisible(false));
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