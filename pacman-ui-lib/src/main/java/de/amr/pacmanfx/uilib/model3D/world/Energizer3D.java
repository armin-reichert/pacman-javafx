/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.world;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelEntity;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
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

public class Energizer3D implements GameLevelEntity, DisposableGraphicsObject {

    public enum AnimationID {
        ENERGIZER_PUMPING;

        public String atTile(Vector2i tile) {
            return "%s_%d_%d".formatted(name(), tile.x(), tile.y());
        }
    }

    private static final PhongMaterial DEFAULT_MATERIAL = new PhongMaterial(Color.WHITE);

    private static Shape3D createDefaultShape() {
        final var shape = new Sphere(3.5);
        shape.setMaterial(DEFAULT_MATERIAL);
        return shape;
    }

    private ManagedAnimation createPumpingAnimation(
        String label,
        Shape3D shape3D,
        int pumpingFrequency,
        double inflatedSize,
        double expandedSize)
    {
        final var animation = new ManagedAnimation(label);
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

    private final AnimationRegistry animations;
    private Vector2i tile;
    private Point3D center;

    private int pumpingFrequency = 3;
    private double inflatedSize = 0.2;
    private double expandedSize = 1.0;

    private Supplier<Shape3D> shapeFactory;
    private Shape3D shape;

    public Energizer3D(AnimationRegistry animations) {
        this.animations = requireNonNull(animations);
        this.shapeFactory = Energizer3D::createDefaultShape;
        setLocation(Vector2i.ZERO, HTS);
    }

    public void setLocation(Vector2i tile, double centerZ) {
        this.tile = requireNonNull(tile);
        final Vector2i centerXY = tile.scaled(TS).plus(HTS, HTS);
        center = new Point3D(centerXY.x(), centerXY.y(), centerZ);
        if (shape != null) {
            updateShapeLocation();
        }
    }

    @Override
    public void init(GameLevel level) {
        stopPumping();
    }

    @Override
    public void dispose() {
        cleanupShape3D(shape);
        shape = null;
        animations.optAnimation(AnimationID.ENERGIZER_PUMPING.atTile(tile)).ifPresent(ManagedAnimation::dispose);
    }

    public Shape3D shape() {
        if (shape == null) {
            shape = shapeFactory.get();
            updateShapeLocation();
            final String animationID = AnimationID.ENERGIZER_PUMPING.atTile(tile);
            animations.optAnimation(animationID).ifPresent(ManagedAnimation::dispose);
            final var pumping = createPumpingAnimation("Energizer Pumping, Tile %s".formatted(tile), shape, pumpingFrequency, inflatedSize, expandedSize);
            animations.register(animationID, pumping);
        }
        return shape;
    }

    private void updateShapeLocation() {
        shape.setTranslateX(center.getX());
        shape.setTranslateY(center.getY());
        shape.setTranslateZ(center.getZ());
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
        animations.optAnimation(AnimationID.ENERGIZER_PUMPING.atTile(tile)).ifPresent(ManagedAnimation::playOrContinue);
    }

    public void stopPumping() {
        animations.optAnimation(AnimationID.ENERGIZER_PUMPING.atTile(tile)).ifPresent(ManagedAnimation::stop);
    }
}
