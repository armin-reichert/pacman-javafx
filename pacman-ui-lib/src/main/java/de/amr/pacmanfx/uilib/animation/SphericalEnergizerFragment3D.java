/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.math.Vector3f;
import javafx.geometry.Point3D;
import javafx.scene.paint.Material;
import javafx.scene.shape.Sphere;

public class SphericalEnergizerFragment3D extends AbstractEnergizerFragment {

    /** Number of subdivisions used for the sphere mesh. */
    private static final short MESH_DIVISIONS = 8;

    private final Sphere sphere;

    /**
     * Creates a new energizer fragment.
     *
     * @param radius   the sphere radius
     * @param material the material applied to the sphere
     * @param velocity the initial velocity vector
     * @param origin   the initial world position of the fragment
     */
    public SphericalEnergizerFragment3D(double radius, Material material, Vector3f velocity, Point3D origin) {
        setVelocity(velocity);
        sphere = new Sphere(radius, MESH_DIVISIONS);
        sphere.setMaterial(material);
        sphere.setTranslateX(origin.getX());
        sphere.setTranslateY(origin.getY());
        sphere.setTranslateZ(origin.getZ());
    }

    @Override
    public Sphere shape() {
        return sphere;
    }

    @Override
    public void dispose() {
        sphere.setMaterial(null);
    }

    @Override
    public void setSize(double size) {
        sphere.setRadius(0.5 * size);
    }

    @Override
    public double size() {
        return 2 * sphere.getRadius();
    }
}
