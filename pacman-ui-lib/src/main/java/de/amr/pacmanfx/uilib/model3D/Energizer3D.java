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
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;

import java.util.function.Supplier;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

public class Energizer3D implements Disposable {

    private static Shape3D createDefaultShape() {
        final var shape = new Sphere(3.5);
        shape.setMaterial(new PhongMaterial(Color.WHITE));
        return shape;
    }

    private static ManagedAnimation createPumpingAnimation(
        AnimationRegistry animationRegistry,
        String label,
        Shape3D shape3D,
        int pumpingFrequency,
        double inflatedSize,
        double expandedSize)
    {
        final var animation = new ManagedAnimation(animationRegistry, label);
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
        Vector2i tile,
        double z)
    {
        this.animationRegistry = requireNonNull(animationRegistry);
        this.tile = requireNonNull(tile);

        final Vector2i tileCenter = tile.scaled(TS).plus(HTS, HTS);
        this.center = new Point3D(tileCenter.x(), tileCenter.y(), z);

        shapeFactory = Energizer3D::createDefaultShape;
    }

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

    public Shape3D shape() {
        if (shape == null) {
            shape = shapeFactory.get();
            shape.setTranslateX(center.getX());
            shape.setTranslateY(center.getY());
            shape.setTranslateZ(center.getZ());
            if (pumpingAnimation != null) {
                pumpingAnimation.dispose();
            }
            pumpingAnimation = createPumpingAnimation(animationRegistry, "Energizer_Pumping_%s".formatted(tile),
                shape, pumpingFrequency, inflatedSize, expandedSize);
        }
        return shape;
    }

    public Vector2i tile() { return tile; }

    public void setShapeFactory(Supplier<Shape3D> shapeFactory) {
        this.shapeFactory = requireNonNull(shapeFactory);
        shape = null; // trigger shape and pumping animation recreation
    }

    public void setInflatedSize(double inflatedSize) {
        this.inflatedSize = inflatedSize;
        shape = null; // trigger shape and pumping animation recreation
    }

    public void setExpandedSize(double expandedSize) {
        this.expandedSize = expandedSize;
        shape = null; // trigger shape and pumping animation recreation
    }

    public void setPumpingFrequency(int frequency) {
        this.pumpingFrequency = frequency;
        shape = null; // trigger shape and pumping animation recreation
    }

    public void hide() {
        if (shape != null) {
            shape.setVisible(false);
        }
    }

    public void startPumping() {
        if (pumpingAnimation != null) {
            pumpingAnimation.playOrContinue();
        }
    }

    public void stopPumping() {
        if (pumpingAnimation != null) {
            pumpingAnimation.stop();
        }
    }

    public void onEaten() {
        stopPumping();
        hide();
    }
}
