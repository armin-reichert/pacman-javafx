/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.animation;

import javafx.geometry.Point3D;
import javafx.scene.paint.Material;
import javafx.scene.shape.Sphere;

public class BallEnergizerParticle extends EnergizerParticle {

    /**
     * Creates a new energizer fragment.
     *
     * @param radius   the sphere radius
     * @param material the material applied to the sphere
     * @param origin   the initial world position of the fragment
     * @param divisions the mesh subdivisions of the sphere
     */
    public BallEnergizerParticle(double radius, Material material, Point3D origin, int divisions) {
        super(new Sphere(radius, divisions));
        final Sphere ball = shape();
        ball.setMaterial(material);
        ball.setTranslateX(origin.getX());
        ball.setTranslateY(origin.getY());
        ball.setTranslateZ(origin.getZ());
    }

    public void changeMeshResolution(int divisions) {
        final Sphere ball = shape();
        if (ball.getDivisions() != divisions) {
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
        setVelocity(null);
        setTargetPosition(null);
    }

    @Override
    public void setSize(double size) {
        shape().setRadius(0.5 * size);
    }

    @Override
    public double size() {
        return 2 * shape().getRadius();
    }

    private Sphere modifiedBall(int divisions) {
        final Sphere ball = shape();
        final Sphere newBall = new Sphere(ball.getRadius(), divisions);
        newBall.setMaterial(ball.getMaterial());
        newBall.setTranslateX(ball.getTranslateX());
        newBall.setTranslateY(ball.getTranslateY());
        newBall.setTranslateZ(ball.getTranslateZ());
        return newBall;
    }
}
