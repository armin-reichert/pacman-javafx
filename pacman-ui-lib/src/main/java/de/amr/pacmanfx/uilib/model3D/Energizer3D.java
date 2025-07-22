/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Destroyable;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;

import static de.amr.pacmanfx.Validations.requireNonNegative;
import static java.util.Objects.requireNonNull;

/**
 * 3D energizer pellet.
 */
public class Energizer3D implements Eatable3D, Destroyable {

    private final Sphere sphere;

    private ManagedAnimation pumpingAnimation;
    private ManagedAnimation eatenAnimation;

    public Energizer3D(AnimationManager animationManager, double radius, double minScaling, double maxScaling) {
        requireNonNegative(radius, "Energizer radius must be positive but is %f");
        requireNonNull(animationManager);

        sphere = new Sphere(radius);

        pumpingAnimation = new ManagedAnimation(animationManager, "Energizer_Pumping") {
            @Override
            protected Animation createAnimation() {
                // 3 full blinks per second
                var scaleTransition = new ScaleTransition(Duration.millis(166.6), sphere);
                scaleTransition.setAutoReverse(true);
                scaleTransition.setCycleCount(Animation.INDEFINITE);
                scaleTransition.setInterpolator(Interpolator.EASE_BOTH);
                scaleTransition.setFromX(maxScaling);
                scaleTransition.setFromY(maxScaling);
                scaleTransition.setFromZ(maxScaling);
                scaleTransition.setToX(minScaling);
                scaleTransition.setToY(minScaling);
                scaleTransition.setToZ(minScaling);
                return scaleTransition;
            }
        };
    }

    public void setEatenAnimation(ManagedAnimation animation) {
        eatenAnimation = requireNonNull(animation);
    }

    @Override
    public void destroy() {
        if (pumpingAnimation != null) {
            pumpingAnimation.stop();
            pumpingAnimation.destroy();
            pumpingAnimation = null;
        }
        if (eatenAnimation != null) {
            eatenAnimation.stop();
            eatenAnimation.destroy();
            eatenAnimation = null;
        }
    }

    public ManagedAnimation pumpingAnimation() {
        return pumpingAnimation;
    }

    @Override
    public Shape3D shape3D() {
        return sphere;
    }

    @Override
    public void onEaten() {
        pumpingAnimation.stop();
        sphere.setVisible(false);
        if (eatenAnimation != null) {
            eatenAnimation.playFromStart();
        }
    }
}