/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.scene3d;

import de.amr.games.pacman.model.actors.Entity;
import javafx.scene.Camera;
import javafx.scene.transform.Rotate;

import static de.amr.games.pacman.lib.Globals.lerp;
import static de.amr.games.pacman.ui3d.PacManGames3dUI.*;

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

        @Override
        public String toString() {
            return "Total";
        }

        @Override
        public void init(Camera cam) {
            cam.setRotationAxis(Rotate.X_AXIS);
            cam.setRotate(TOTAL_ROTATE);
            cam.setTranslateX(TOTAL_TRANSLATE_X);
            cam.setTranslateY(TOTAL_TRANSLATE_Y);
            cam.setTranslateZ(TOTAL_TRANSLATE_Z);
        }
    },

    FOLLOWING_PLAYER() {
        @Override
        public String toString() {
            return "Following Player";
        }

        @Override
        public void init(Camera cam) {
            cam.setRotationAxis(Rotate.X_AXIS);
            cam.setRotate(60);
            cam.setTranslateZ(-160);
        }

        @Override
        public void update(Camera cam, Entity spottedEntity) {
            double speedX = 0.03;
            double speedY = 0.06;
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

    public static Perspective next(Perspective p) {
        int n = Perspective.values().length;
        return Perspective.values()[p.ordinal() < n - 1 ? p.ordinal() + 1 : 0];
    }

    public static Perspective previous(Perspective p) {
        int n = Perspective.values().length;
        return Perspective.values()[p.ordinal() > 0 ? p.ordinal() - 1 : n - 1];
    }
}