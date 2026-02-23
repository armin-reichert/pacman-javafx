/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.math.Vector3f;
import javafx.scene.paint.Material;
import javafx.scene.shape.Sphere;

public class BallEnergizerParticle extends EnergizerParticle {

    /**
     * Creates a new energizer fragment.
     *
     * @param radius   the sphere radius
     * @param material the material applied to the sphere
     * @param center   the initial position of the fragment
     * @param divisions the mesh subdivisions of the sphere
     */
    public BallEnergizerParticle(double radius, Material material, Vector3f center, int divisions) {
        super(new Sphere(radius, divisions));
        setPosition(center);
        shape().setMaterial(material);
    }

    public void changeMeshResolution(int divisions) {
        if (shape().getDivisions() != divisions) {
            setShape3D(modifiedBall(divisions));
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

    private Sphere modifiedBall(int meshDivisions) {
        final Sphere oldBall = shape();
        final Sphere newBall = new Sphere(oldBall.getRadius(), meshDivisions);
        newBall.setMaterial(oldBall.getMaterial());
        return newBall;
    }
}
