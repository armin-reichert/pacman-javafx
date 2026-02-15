/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.animation;

import javafx.geometry.Point3D;
import javafx.scene.paint.Material;
import javafx.scene.shape.Sphere;

public class BallEnergizerFragment extends AbstractEnergizerFragment {

    private Sphere ball;

    /**
     * Creates a new energizer fragment.
     *
     * @param radius   the sphere radius
     * @param material the material applied to the sphere
     * @param origin   the initial world position of the fragment
     * @param divisions the mesh subdivisions of the sphere
     */
    public BallEnergizerFragment(double radius, Material material, Point3D origin, int divisions) {
        ball = new Sphere(radius, divisions);
        ball.setMaterial(material);
        ball.setTranslateX(origin.getX());
        ball.setTranslateY(origin.getY());
        ball.setTranslateZ(origin.getZ());
    }

    private Sphere modifiedBall(int divisions) {
        final Sphere newBall = new Sphere(ball.getRadius(), divisions);
        newBall.setMaterial(ball.getMaterial());
        newBall.setTranslateX(ball.getTranslateX());
        newBall.setTranslateY(ball.getTranslateY());
        newBall.setTranslateZ(ball.getTranslateZ());
        return newBall;
    }

    public void changeBallMeshResolution(int divisions) {
        if (ball.getDivisions() != divisions) {
            ball = modifiedBall(divisions);
        }
    }

    @Override
    public Sphere shape() {
        return ball;
    }

    @Override
    public void dispose() {
        ball.setMaterial(null);
        setVelocity(null);
        setTargetPosition(null);
    }

    @Override
    public void setSize(double size) {
        ball.setRadius(0.5 * size);
    }

    @Override
    public double size() {
        return 2 * ball.getRadius();
    }
}
