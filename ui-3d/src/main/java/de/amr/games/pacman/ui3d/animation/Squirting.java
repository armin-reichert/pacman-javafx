/*
MIT License

Copyright (c) 2021-2024 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package de.amr.games.pacman.ui3d.animation;

import javafx.animation.Transition;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Material;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * @author Armin Reichert
 */
public abstract class Squirting extends Transition {

    public static class Drop extends Sphere {
        private double vx, vy, vz;

        Drop(Material material, double radius, Point3D origin) {
            super(radius);
            setMaterial(material);
            setTranslateX(origin.getX());
            setTranslateY(origin.getY());
            setTranslateZ(origin.getZ());
        }

        void setVelocity(double x, double y, double z) {
            vx = x;
            vy = y;
            vz = z;
        }

        void move(Point3D gravity) {
            setTranslateX(getTranslateX() + vx);
            setTranslateY(getTranslateY() + vy);
            setTranslateZ(getTranslateZ() + vz);
            vx += gravity.getX();
            vy += gravity.getY();
            vz += gravity.getZ();
        }
    }

    private final Group root = new Group();
    private Point3D gravity = new Point3D(0, 0, 0.1f);
    private float dropRadiusMin = 0.1f;
    private float dropRadiusMax = 1.0f;
    private Point3D dropVelocityMin = new Point3D(-0.25f, -0.25f, -4.0f);
    private Point3D dropVelocityMax = new Point3D(0.25f, 0.25f, -1.0f);

    protected Squirting() {
        setCycleDuration(Duration.seconds(2));
    }

    protected abstract boolean reachedFinalPosition(Drop drop);

    public Group root() {
        return root;
    }

    public Point3D getGravity() {
        return gravity;
    }

    public void setGravity(Point3D gravity) {
        this.gravity = gravity;
    }

    public float getDropRadiusMin() {
        return dropRadiusMin;
    }

    public void setDropRadiusMin(float dropRadiusMin) {
        this.dropRadiusMin = dropRadiusMin;
    }

    public float getDropRadiusMax() {
        return dropRadiusMax;
    }

    public void setDropRadiusMax(float dropRadiusMax) {
        this.dropRadiusMax = dropRadiusMax;
    }

    public Point3D getDropVelocityMin() {
        return dropVelocityMin;
    }

    public void setDropVelocityMin(Point3D dropVelocityMin) {
        this.dropVelocityMin = dropVelocityMin;
    }

    public Point3D getDropVelocityMax() {
        return dropVelocityMax;
    }

    public void setDropVelocityMax(Point3D dropVelocityMax) {
        this.dropVelocityMax = dropVelocityMax;
    }

    public void createDrops(int min, int maxEx, Material material, Point3D origin) {
        for (int i = 0; i < randomInt(min, maxEx); ++i) {
            var drop = new Drop(material, randomFloat(dropRadiusMin, dropRadiusMax), origin);
            drop.setVisible(false);
            drop.setVelocity(
                randomDouble(dropVelocityMin.getX(), dropVelocityMax.getX()),
                randomDouble(dropVelocityMin.getY(), dropVelocityMax.getY()),
                randomDouble(dropVelocityMin.getZ(), dropVelocityMax.getZ()));
            root.getChildren().add(drop);
        }
        Logger.info("{} drops created", root.getChildren().size());
    }

    @Override
    protected void interpolate(double t) {
        Logger.info("t={}", t);
        if (t == 0.0) {
            //TODO why is this never called?
            Logger.info("First interpolation frame");
            return;
        }
        if (t >= 1.0) {
            Logger.info("Last interpolation frame");
            return;
        }
        for (var drops : root.getChildren()) {
            var drop = (Drop) drops;
            drop.setVisible(true);
            if (reachedFinalPosition(drop)) {
                drop.setVelocity(0, 0, 0);
                drop.setScaleZ(0.1);
            } else {
                drop.move(gravity);
            }
        }
    }
}