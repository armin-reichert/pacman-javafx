/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.scene3d;

import de.amr.games.pacman.model.actors.Entity;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
        public void init(Camera cam) {
            unbind(cam);
            cam.setRotationAxis(Rotate.X_AXIS);
            cam.setRotate(0);
            cam.setTranslateX(0);
            cam.setTranslateY(0);
            cam.setTranslateZ(-400);
        }

        @Override
        public void update(Camera cam, Entity spottedEntity) {
            var position = spottedEntity.position();
            double speed = 0.01;
            cam.setTranslateX(lerp(cam.getTranslateX(), position.x() - 100, speed));
            cam.setTranslateY(lerp(cam.getTranslateY(), position.y() - 150, speed));
        }
    },

    TOTAL {
        private boolean initialized;

        @Override
        public String toString() {
            return "Total";
        }

        @Override
        public void init(Camera cam) {
            cam.setRotationAxis(Rotate.X_AXIS);
            bind(cam);
            if (!initialized) {
                rotatePy().set(66);
                translateXPy().set(0);
                translateYPy().set(330);
                translateZPy().set(-140);
                initialized = true;
            }
        }
    },

    FOLLOWING_PLAYER() {
        @Override
        public String toString() {
            return "Following Player";
        }

        @Override
        public void init(Camera cam) {
            unbind(cam);
            cam.setRotationAxis(Rotate.X_AXIS);
            cam.setRotate(60);
            cam.setTranslateZ(-160);
        }

        @Override
        public void update(Camera cam, Entity spottedEntity) {
            double speedX = 0.005;
            double speedY = 0.030;
            cam.setTranslateX(lerp(cam.getTranslateX(), spottedEntity.position().x() - 100, speedX));
            cam.setTranslateY(lerp(cam.getTranslateY(), spottedEntity.position().y() + 100, speedY));
        }
    },

    NEAR_PLAYER() {
        @Override
        public String toString() {
            return "Near Player";
        }

        @Override
        public void init(Camera cam) {
            unbind(cam);
            cam.setRotationAxis(Rotate.X_AXIS);
            cam.setRotate(80);
            cam.setTranslateZ(-40);
        }

        @Override
        public void update(Camera cam, Entity spottedEntity) {
            double speed = 0.02;
            cam.setTranslateX(lerp(cam.getTranslateX(), spottedEntity.position().x() - 110, speed));
            cam.setTranslateY(lerp(cam.getTranslateY(), spottedEntity.position().y(), speed));
        }
    };

    final IntegerProperty rotatePy     = new SimpleIntegerProperty(this, "rotate");
    final IntegerProperty translateXPy = new SimpleIntegerProperty(this, "translateX");
    final IntegerProperty translateYPy = new SimpleIntegerProperty(this, "translateY");
    final IntegerProperty translateZPy = new SimpleIntegerProperty(this, "translateZ");

    protected void bind(Camera cam) {
        cam.rotateProperty().bind(rotatePy);
        cam.translateXProperty().bind(translateXPy);
        cam.translateYProperty().bind(translateYPy);
        cam.translateZProperty().bind(translateZPy);
    }

    protected void unbind(Camera cam) {
        cam.rotateProperty().unbind();
        cam.translateXProperty().unbind();
        cam.translateYProperty().unbind();
        cam.translateZProperty().unbind();
    }

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

    public static Perspective next(Perspective p) {
        int n = Perspective.values().length;
        return Perspective.values()[p.ordinal() < n - 1 ? p.ordinal() + 1 : 0];
    }

    public static Perspective previous(Perspective p) {
        int n = Perspective.values().length;
        return Perspective.values()[p.ordinal() > 0 ? p.ordinal() - 1 : n - 1];
    }
}