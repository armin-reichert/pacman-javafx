/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.scene3d;

import de.amr.games.pacman.ui.fx.v3d.entity.Pac3D;
import javafx.beans.property.*;
import javafx.scene.Camera;
import javafx.scene.transform.Rotate;

import static de.amr.games.pacman.lib.Globals.lerp;

/**
 * Play scene perspectives.
 *
 * @author Armin Reichert
 */
public enum Perspective implements CameraController {

    DRONE {
        @Override
        public String toString() {
            return "Drone";
        }

        @Override
        public void reset(Camera cam) {
            cam.setNearClip(0.1);
            cam.setFarClip(10000.0);
            cam.setRotationAxis(Rotate.X_AXIS);
            cam.setRotate(0);
            cam.setTranslateX(0);
            cam.setTranslateY(0);
            cam.setTranslateZ(-400);
        }

        @Override
        public void update(Camera cam, Pac3D pac3D) {
            var position = pac3D.position();
            double speed = 0.01;
            cam.setTranslateX(lerp(cam.getTranslateX(), position.getX() - 100, speed));
            cam.setTranslateY(lerp(cam.getTranslateY(), position.getY() - 150, speed));
        }
    },

    TOTAL {

        private boolean initialized;

        @Override
        public void reset(Camera cam) {
            cam.setNearClip(0.1);
            cam.setFarClip(10000.0);
            cam.setRotationAxis(Rotate.X_AXIS);
            if (!initialized) {
                rotatePy().set(66);
                translateXPy().set(0);
                translateYPy().set(330);
                translateZPy().set(-140);
                initialized = true;
            }
        }

        @Override
        public void update(Camera cam, Pac3D pac3D) {
            // cam properties cannot be bound and be set at the same time
            cam.rotateProperty().set(rotatePy().get());
            cam.translateXProperty().set(translateXPy().get());
            cam.translateYProperty().set(translateYPy().get());
            cam.translateZProperty().set(translateZPy().get());
        }

        @Override
        public String toString() {
            return "Total";
        }
    },

    FOLLOWING_PLAYER() {
        @Override
        public String toString() {
            return "Following Player";
        }

        @Override
        public void reset(Camera cam) {
            cam.setNearClip(0.1);
            cam.setFarClip(10000.0);
            cam.setRotationAxis(Rotate.X_AXIS);
            cam.setRotate(60);
            cam.setTranslateZ(-160);
        }

        @Override
        public void update(Camera cam, Pac3D pac3D) {
            var position = pac3D.position();
            double speedX = 0.005;
            cam.setTranslateX(lerp(cam.getTranslateX(), position.getX() - 100, speedX));
            double speedY = 0.030;
            cam.setTranslateY(lerp(cam.getTranslateY(), position.getY() + 100, speedY));
        }
    },

    NEAR_PLAYER() {
        @Override
        public String toString() {
            return "Near Player";
        }

        @Override
        public void reset(Camera cam) {
            cam.setNearClip(0.1);
            cam.setFarClip(10000.0);
            cam.setRotationAxis(Rotate.X_AXIS);
            cam.setRotate(80);
            cam.setTranslateZ(-40);
        }

        @Override
        public void update(Camera cam, Pac3D pac3D) {
            var position = pac3D.position();
            double speed = 0.02;
            cam.setTranslateX(lerp(cam.getTranslateX(), position.getX() - 110, speed));
            cam.setTranslateY(lerp(cam.getTranslateY(), position.getY(), speed));
        }
    };

    final IntegerProperty rotatePy = new SimpleIntegerProperty();
    final IntegerProperty translateXPy = new SimpleIntegerProperty(this, "translateX");
    final IntegerProperty translateYPy = new SimpleIntegerProperty(this, "translateY");
    final IntegerProperty translateZPy = new SimpleIntegerProperty(this, "translateZ");

    @Override
    public IntegerProperty rotatePy() {
        return rotatePy;
    }

    @Override
    public IntegerProperty translateXPy() {
        return translateXPy;
    }

    @Override
    public IntegerProperty translateYPy() {
        return translateYPy;
    }

    @Override
    public IntegerProperty translateZPy() {
        return translateZPy;
    }

    public static Perspective succ(Perspective p) {
        int n = Perspective.values().length;
        return Perspective.values()[p.ordinal() < n - 1 ? p.ordinal() + 1 : 0];
    }

    public static Perspective pred(Perspective p) {
        int n = Perspective.values().length;
        return Perspective.values()[p.ordinal() > 0 ? p.ordinal() - 1 : n - 1];
    }
}