/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.animation;

import javafx.scene.paint.Material;
import javafx.scene.shape.Sphere;

import static java.util.Objects.requireNonNull;

public class SphericalEnergizerParticle extends EnergizerParticle {

    public enum Resolution { LOW, HIGH;

        public int divisions() {
            return switch (this) {
                case LOW -> 4;
                case HIGH -> 8;
            };
        }
    }

    /**
     * Creates a new energizer fragment.
     *
     * @param radius   the sphere radius
     * @param material the material applied to the sphere
     * @param resolution the mesh resolution
     */
    public SphericalEnergizerParticle(double radius, Material material, Resolution resolution) {
        super(new Sphere(radius, resolution.divisions()));
        shape().setMaterial(material);
    }

    public void setResolution(Resolution resolution) {
        requireNonNull(resolution);
        if (shape().getDivisions() != resolution.divisions()) {
            setShape3D(reshapedSphere(resolution));
        }
    }

    @Override
    public Sphere shape() {
        return (Sphere) super.shape();
    }

    @Override
    public void dispose() {
        shape().setMaterial(null);
    }

    @Override
    public void setSize(double size) {
        shape().setRadius(0.5 * size);
    }

    @Override
    public double size() {
        return 2 * shape().getRadius();
    }

    private Sphere reshapedSphere(Resolution resolution) {
        requireNonNull(resolution);
        final Sphere oldShape = shape();
        final Sphere newShape = new Sphere(oldShape.getRadius(), resolution.divisions());
        newShape.setMaterial(oldShape.getMaterial());
        return newShape;
    }
}
