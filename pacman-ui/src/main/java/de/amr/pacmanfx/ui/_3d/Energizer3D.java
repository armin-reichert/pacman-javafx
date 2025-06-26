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

    private final AnimationManager animationManager;
    private ScaleTransition pumpingAnimation;
    private Animation hideAnimation;
    private Animation hideAndEatAnimation;
    private Animation eatenAnimation;

    public Energizer3D(double radius, AnimationManager animationManager) {
        this.animationManager = requireNonNull(animationManager);
        requireNonNegative(radius, "Energizer radius must be positive but is %f");
        sphere = new Sphere(radius);
    }

    public void setEatenAnimation(Animation eatenAnimation) {
        this.eatenAnimation = requireNonNull(eatenAnimation);
    }

    @Override
    public void onEaten() {
        stopPumpingAnimation();
        if (eatenAnimation != null) {
            playHideAndEatAnimation();
        } else {
            playHideAnimation();
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

    private ScaleTransition createPumpingAnimation() {
        // 3 full blinks per second
        var animation = new ScaleTransition(Duration.millis(166.6), sphere);
        animation.setAutoReverse(true);
        animation.setCycleCount(Animation.INDEFINITE);
        animation.setInterpolator(Interpolator.EASE_BOTH);
        animation.setFromX(Settings3D.ENERGIZER_3D_MAX_SCALING);
        animation.setFromY(Settings3D.ENERGIZER_3D_MAX_SCALING);
        animation.setFromZ(Settings3D.ENERGIZER_3D_MAX_SCALING);
        animation.setToX(Settings3D.ENERGIZER_3D_MIN_SCALING);
        animation.setToY(Settings3D.ENERGIZER_3D_MIN_SCALING);
        animation.setToZ(Settings3D.ENERGIZER_3D_MIN_SCALING);
        return animation;
    }

    public void startPumpingAnimation() {
        if (pumpingAnimation == null) {
            pumpingAnimation = createPumpingAnimation();
            animationManager.register("Energizer_Pumping", pumpingAnimation);
        }
        pumpingAnimation.playFromStart();
    }

    public void stopPumpingAnimation() {
        if (pumpingAnimation != null) {
            pumpingAnimation.stop();
        }
    }

    private Animation createHideAnimation() {
        var hideAnimation = new PauseTransition(Duration.seconds(0.05));
        hideAnimation.setOnFinished(e -> shape3D().setVisible(false));
        return hideAnimation;
    }

    private void playHideAnimation() {
        if (hideAnimation == null) {
            hideAnimation = createHideAnimation();
            animationManager.register("Energizer_Hide", hideAnimation);
        }
        hideAnimation.play();
    }

    private Animation createHideAndEatAnimation() {
        if (eatenAnimation == null) {
            return createHideAnimation();
        } else {
            return new SequentialTransition(createHideAnimation(), eatenAnimation);
        }
    }

    private void playHideAndEatAnimation() {
        if (hideAndEatAnimation == null) {
            hideAndEatAnimation = createHideAndEatAnimation();
            animationManager.register("Energizer_HideAndEat", hideAndEatAnimation);
        }
        hideAndEatAnimation.playFromStart();
    }

}