/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.RegisteredAnimation;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.geometry.Point3D;
import javafx.scene.paint.Material;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;

import static de.amr.pacmanfx.Validations.requireNonNegative;
import static java.util.Objects.requireNonNull;

public class SphericalEnergizer3D implements Energizer3D {

    private final Sphere sphere;
    private RegisteredAnimation pumpingAnimation;
    private RegisteredAnimation eatenAnimation;

    public SphericalEnergizer3D(
        AnimationRegistry animationRegistry,
        double radius,
        Point3D center,
        double minScaling,
        double maxScaling,
        Material material,
        Vector2i tile)
    {
        requireNonNegative(radius, "Energizer radius must be positive but is %f");
        requireNonNull(animationRegistry);

        sphere = new Sphere(radius);
        sphere.setMaterial(material);
        sphere.setTranslateX(center.getX());
        sphere.setTranslateY(center.getY());
        sphere.setTranslateZ(center.getZ());

        sphere.setUserData(requireNonNull(tile));

        pumpingAnimation = new RegisteredAnimation(animationRegistry, "Energizer_Pumping") {
            @Override
            protected Animation createAnimationFX() {
                // 3 full pumping cycles per second = 6 compress/expand cycles
                Duration duration = Duration.millis(166.6);
                var transition = new ScaleTransition(duration, sphere);
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
    public void hide() {
        sphere.setVisible(false);
    }

    @Override
    public Sphere shape() { return sphere; }

    @Override
    public Vector2i tile() { return (Vector2i) sphere.getUserData(); }

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

    public void startPumping() {
        pumpingAnimation.playOrContinue();
    }

    public void stopPumping() {
        pumpingAnimation.pause();
    }

    public void setEatenAnimation(RegisteredAnimation animation) {
        eatenAnimation = requireNonNull(animation);
    }

    public void onEaten() {
        pumpingAnimation.stop();
        sphere.setVisible(false);
        if (eatenAnimation != null) {
            eatenAnimation.playFromStart();
        }
    }
}
