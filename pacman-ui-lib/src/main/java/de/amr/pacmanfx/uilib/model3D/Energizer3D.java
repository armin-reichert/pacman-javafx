/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.geometry.Point3D;
import javafx.scene.paint.Material;
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
    private ManagedAnimation eatenAnimation;

    public Energizer3D(AnimationRegistry animationRegistry, double radius, Point3D center, double minScaling, double maxScaling, Material material) {
        requireNonNegative(radius, "Energizer radius must be positive but is %f");
        requireNonNull(animationRegistry);

        setRadius(radius);
        setMaterial(material);
        setTranslateX(center.getX());
        setTranslateY(center.getY());
        setTranslateZ(center.getZ());

        pumpingAnimation = new ManagedAnimation(animationRegistry, "Energizer_Pumping") {
            @Override
            protected Animation createAnimationFX() {
                // 3 full pumping cycles per second = 6 compress/expand cycles
                Duration duration = Duration.millis(166.6);
                var transition = new ScaleTransition(duration, Energizer3D.this);
                transition.setAutoReverse(true);
                transition.setCycleCount(Animation.INDEFINITE);
                transition.setInterpolator(Interpolator.EASE_BOTH);
                transition.setFromX(maxScaling);
                transition.setFromY(maxScaling);
                transition.setFromZ(maxScaling);
                transition.setToX(minScaling);
                transition.setToY(minScaling);
                transition.setToZ(minScaling);
                return transition;
            }
        };
    }

    @Override
    public void dispose() {
        if (pumpingAnimation != null) {
            pumpingAnimation.dispose();
            pumpingAnimation = null;
        }
        if (eatenAnimation != null) {
            eatenAnimation.dispose();
            eatenAnimation = null;
        }
    }

    public void playPumping() {
        pumpingAnimation.playOrContinue();
    }

    public void pausePumping() {
        pumpingAnimation.pause();
    }

    public void setEatenAnimation(ManagedAnimation animation) {
        eatenAnimation = requireNonNull(animation);
    }

    @Override
    public Shape3D node() {
        return this;
    }

    @Override
    public void onEaten() {
        pumpingAnimation.stop();
        setVisible(false);
        if (eatenAnimation != null) {
            eatenAnimation.playFromStart();
        }
    }
}