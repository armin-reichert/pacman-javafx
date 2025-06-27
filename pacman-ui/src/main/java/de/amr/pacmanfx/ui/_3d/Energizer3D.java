/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
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
    private final ManagedAnimation pumpingAnimation;
    private final ManagedAnimation hideAndEatAnimation;
    private Animation eatenAnimation;

    /**
     * @param radius radius of sphere (positive number)
     * @param animationManager the animation manager
     */
    public Energizer3D(double radius, AnimationManager animationManager) {
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
                scaleTransition.setFromX(Settings3D.ENERGIZER_3D_MAX_SCALING);
                scaleTransition.setFromY(Settings3D.ENERGIZER_3D_MAX_SCALING);
                scaleTransition.setFromZ(Settings3D.ENERGIZER_3D_MAX_SCALING);
                scaleTransition.setToX(Settings3D.ENERGIZER_3D_MIN_SCALING);
                scaleTransition.setToY(Settings3D.ENERGIZER_3D_MIN_SCALING);
                scaleTransition.setToZ(Settings3D.ENERGIZER_3D_MIN_SCALING);
                return scaleTransition;
            }
        };

        hideAndEatAnimation = new ManagedAnimation(animationManager, "Energizer_Hide") {
            @Override
            protected Animation createAnimation() {
                Animation hide = Ufx.doAfterSec(0.05, () -> shape3D().setVisible(false));
                return eatenAnimation == null? hide : new SequentialTransition(hide, eatenAnimation);
            }
        };
    }

    public ManagedAnimation pumpingAnimation() {
        return pumpingAnimation;
    }

    public void setEatenAnimation(Animation eatenAnimation) {
        this.eatenAnimation = requireNonNull(eatenAnimation);
        hideAndEatAnimation.invalidate();
    }

    @Override
    public void onEaten() {
        pumpingAnimation.stop();
        hideAndEatAnimation.play(ManagedAnimation.FROM_START);
    }

    @Override
    public Shape3D shape3D() {
        return sphere;
    }
}