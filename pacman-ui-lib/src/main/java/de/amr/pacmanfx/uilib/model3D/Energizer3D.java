/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class Energizer3D implements Disposable {

    private static Shape3D createDefaultShape(Vector2i tile) {
        final var shape = new Sphere(3.5);
        shape.setMaterial(Ufx.coloredPhongMaterial(Color.WHITE));
        shape.setUserData(tile);
        return shape;
    }

    private static ManagedAnimation createPumpingAnimation(
        AnimationRegistry animationRegistry,
        Shape3D shape3D,
        int pumpingFrequency,
        double inflatedSize,
        double expandedSize)
    {
        final var animation = new ManagedAnimation(animationRegistry, "Energizer_Pumping_%s".formatted(shape3D.getUserData()));
        animation.setFactory(() -> {
            final Duration duration = Duration.seconds(1).divide(2 * pumpingFrequency);
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

    private final AnimationRegistry animationRegistry;
    private final Point3D center;
    private final Vector2i tile;

    private int pumpingFrequency = 3;
    private double inflatedSize = 0.2;
    private double expandedSize = 1.0;

    private Supplier<Shape3D> shapeFactory;
    private Shape3D shape;
    private ManagedAnimation pumpingAnimation;

    public Energizer3D(
        AnimationRegistry animationRegistry,
        Point3D center,
        Vector2i tile)
    {
        this.animationRegistry = requireNonNull(animationRegistry);
        this.center = requireNonNull(center);
        this.tile = requireNonNull(tile);
        shapeFactory = () -> createDefaultShape(tile);
    }

    public void setShapeFactory(Supplier<Shape3D> shapeFactory) {
        this.shapeFactory = requireNonNull(shapeFactory);
    }

    public void setInflatedSize(double inflatedSize) {
        this.inflatedSize = inflatedSize;
    }

    public void setExpandedSize(double expandedSize) {
        this.expandedSize = expandedSize;
    }

    public void setPumpingFrequency(int frequency) {
        this.pumpingFrequency = frequency;
    }

    public void hide() {
        shape.setVisible(false);
    }

    public Shape3D shape() {
        if (shape == null) {
            shape = shapeFactory.get();
            shape.setTranslateX(center.getX());
            shape.setTranslateY(center.getY());
            shape.setTranslateZ(center.getZ());
            shape.setUserData(tile);
            pumpingAnimation = createPumpingAnimation(animationRegistry, shape, pumpingFrequency, inflatedSize, expandedSize);
        }
        return shape;
    }

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
