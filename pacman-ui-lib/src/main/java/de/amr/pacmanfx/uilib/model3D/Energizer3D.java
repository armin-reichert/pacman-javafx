/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.math.Vector2i;
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

public class Energizer3D implements Disposable {

    private static final int PUMPING_FREQUENCY = 3; // 3 inflate+expand cycles per second

    private static ManagedAnimation createPumpingAnimation(
        AnimationRegistry animationRegistry,
        Shape3D shape3D,
        double inflatedSize,
        double expandedSize)
    {
        final var animation = new ManagedAnimation(animationRegistry, "Energizer_Pumping_%s".formatted(shape3D.getUserData()));
        animation.setFactory(() -> {
            final Duration duration = Duration.seconds(1).divide(2 * PUMPING_FREQUENCY);
            final var pumping = new ScaleTransition(duration, shape3D);
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

    private Shape3D shape;
    private ManagedAnimation pumpingAnimation;

    public Energizer3D(
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

        shape = new Sphere(radius);
        shape.setMaterial(material);
        shape.setTranslateX(center.getX());
        shape.setTranslateY(center.getY());
        shape.setTranslateZ(center.getZ());
        shape.setUserData(tile);

        pumpingAnimation = createPumpingAnimation(animationRegistry, shape, inflatedSize, expandedSize);
    }

    public void hide() {
        shape.setVisible(false);
    }

    public Shape3D shape() { return shape; }

    public Vector2i tile() { return (Vector2i) shape.getUserData(); }

    @Override
    public void dispose() {
        if (shape != null) {
            shape.setMaterial(null);
            shape = null;
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
        shape.setVisible(false);
    }
}
