/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.ui.PacManGames_UI;
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
public class Energizer3D extends Sphere implements Eatable3D {
    private ManagedAnimation pumpingAnimation;
    private ManagedAnimation hideAndEatAnimation;

    public Energizer3D(double radius, AnimationManager animationManager) {
        setRadius(requireNonNegative(radius, "Energizer radius must be positive but is %f"));

        requireNonNull(animationManager);

        pumpingAnimation = new ManagedAnimation(animationManager, "Energizer_Pumping") {
            @Override
            protected Animation createAnimation() {
                // 3 full blinks per second
                var scaleTransition = new ScaleTransition(Duration.millis(166.6), shape3D());
                scaleTransition.setAutoReverse(true);
                scaleTransition.setCycleCount(Animation.INDEFINITE);
                scaleTransition.setInterpolator(Interpolator.EASE_BOTH);
                scaleTransition.setFromX(PacManGames_UI.ENERGIZER_3D_MAX_SCALING);
                scaleTransition.setFromY(PacManGames_UI.ENERGIZER_3D_MAX_SCALING);
                scaleTransition.setFromZ(PacManGames_UI.ENERGIZER_3D_MAX_SCALING);
                scaleTransition.setToX(PacManGames_UI.ENERGIZER_3D_MIN_SCALING);
                scaleTransition.setToY(PacManGames_UI.ENERGIZER_3D_MIN_SCALING);
                scaleTransition.setToZ(PacManGames_UI.ENERGIZER_3D_MIN_SCALING);
                return scaleTransition;
            }
        };
    }

    public void setHideAndEatAnimation(ManagedAnimation hideAndEatAnimation) {
        this.hideAndEatAnimation = requireNonNull(hideAndEatAnimation);
    }

    public void destroy() {
        pumpingAnimation.destroy();
        pumpingAnimation = null;
        hideAndEatAnimation.destroy();
        hideAndEatAnimation = null;
    }

    public ManagedAnimation pumpingAnimation() {
        return pumpingAnimation;
    }

    @Override
    public Shape3D shape3D() {
        return this;
    }

    @Override
    public void onEaten() {
        pumpingAnimation.stop();
        hideAndEatAnimation.playFromStart();
    }
}