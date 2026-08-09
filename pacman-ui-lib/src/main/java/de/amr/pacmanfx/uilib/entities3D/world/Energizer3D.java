/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.world;

import de.amr.basics.Named;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.uilib.DisposableGraphicsObject;
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

import static java.util.Objects.requireNonNull;

public class Energizer3D implements DisposableGraphicsObject {

    public enum AnimationID implements Named {
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

    private Vector2i tile;
    private Point3D center;

    private Supplier<Shape3D> shapeFactory;
    private Shape3D shape;

    public Energizer3D() {
        this.shapeFactory = Energizer3D::createDefaultShape;
        setLocation(Vector2i.ZERO, WorldMap.HTS);
    }

    public void setLocation(Vector2i tile, double centerZ) {
        this.tile = requireNonNull(tile);
        final Vector2i centerXY = tile.scaled(WorldMap.TS).plus(WorldMap.HTS, WorldMap.HTS);
        center = new Point3D(centerXY.x(), centerXY.y(), centerZ);
        if (shape != null) {
            updateShapeLocation();
        }
    }

    @Override
    public void dispose() {
        cleanupShape3D(shape);
        shape = null;
    }

    public Shape3D shape() {
        if (shape == null) {
            shape = shapeFactory.get();
            updateShapeLocation();
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

    public void hide() {
        if (shape != null) {
            shape.setVisible(false);
        }
    }

}
