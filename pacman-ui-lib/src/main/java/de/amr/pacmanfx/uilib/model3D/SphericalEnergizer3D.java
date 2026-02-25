/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
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

    private static final int PUMPING_FREQUENCY = 3; // 3 inflate+expand cycles per second

    private Sphere sphere;
    private ManagedAnimation pumpingAnimation;

    public SphericalEnergizer3D(
        AnimationRegistry animationRegistry,
        double radius,
        Point3D center,
        double inflatedSize,
        double expandedSize,
        Material material,
        Vector2i tile)
    {
        requireNonNull(animationRegistry);
        requireNonNegative(radius, "Energizer radius must be positive but is %f");
        requireNonNull(center);
        requireNonNegative(inflatedSize, "Energizer inflated size must be positive but is %f");
        requireNonNegative(expandedSize, "Energizer expanded size must be positive but is %f");
        requireNonNull(material);
        requireNonNull(tile);

        sphere = new Sphere(radius);
        sphere.setMaterial(material);
        sphere.setTranslateX(center.getX());
        sphere.setTranslateY(center.getY());
        sphere.setTranslateZ(center.getZ());
        sphere.setUserData(tile);

        pumpingAnimation = createPumpingAnimation(animationRegistry, sphere, inflatedSize, expandedSize);
    }

    private static ManagedAnimation createPumpingAnimation(
        AnimationRegistry animationRegistry,
        Sphere sphere,
        double inflatedSize,
        double expandedSize)
    {
        final var animation = new ManagedAnimation(animationRegistry, "Energizer_Pumping_%s".formatted(sphere.getUserData()));
        animation.setFactory(() -> {
            final Duration duration = Duration.seconds(1).divide(2 * PUMPING_FREQUENCY);
            final var pumping = new ScaleTransition(duration, sphere);
            pumping.setAutoReverse(true);
            pumping.setCycleCount(Animation.INDEFINITE);
            pumping.setInterpolator(Interpolator.EASE_BOTH);
            pumping.setFromX(expandedSize);
            pumping.setFromY(expandedSize);
            pumping.setFromZ(expandedSize);
            pumping.setToX(inflatedSize);
            pumping.setToY(inflatedSize);
            pumping.setToZ(inflatedSize);
            return pumping;
        });
        return animation;
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
        if (sphere != null) {
            sphere.setMaterial(null);
            sphere = null;
        }
        if (pumpingAnimation != null) {
            pumpingAnimation.dispose();
            pumpingAnimation = null;
        }
    }

    public void startPumping() {
        if (pumpingAnimation != null) {
            pumpingAnimation.playOrContinue();
        }
    }

    public void stopPumping() {
        if (pumpingAnimation != null) {
            pumpingAnimation.pause();
        }
    }

    public void onEaten() {
        if (pumpingAnimation != null) {
            pumpingAnimation.stop();
            pumpingAnimation.dispose();
            pumpingAnimation = null;
        }
        sphere.setVisible(false);
    }
}
