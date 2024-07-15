/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.scene;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Entity;
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
        public void init(Camera cam, GameWorld world) {
            cam.setRotationAxis(Rotate.X_AXIS);
            cam.setRotate(0);
            cam.setTranslateX(0);
            cam.setTranslateY(0);
            cam.setTranslateZ(-500);
        }

        @Override
        public void update(Camera cam, GameWorld world, Entity spottedEntity) {
            var position = spottedEntity.position();
            double speed = 0.01;
            double x = lerp(cam.getTranslateX(), position.x(), speed);
            double y = lerp(cam.getTranslateY(), position.y(), speed);
            cam.setTranslateZ(-500);
            cam.setTranslateX(x);
            cam.setTranslateY(y);
        }
    },

    TOTAL {

        @Override
        public String toString() {
            return "Total";
        }

        @Override
        public void init(Camera cam, GameWorld world) {
            cam.setRotationAxis(Rotate.X_AXIS);
            cam.setRotate(70);
            cam.setTranslateX(111);
            int numRows = world != null ? world.map().terrain().numRows() : GameModel.ARCADE_MAP_SIZE_Y;
            cam.setTranslateY( 8.5 * numRows + 175);
            cam.setTranslateZ(-120);
        }

        @Override
        public void update(Camera cam, GameWorld world, Entity spottedEntity) {
            init(cam, world);
        }
    },

    FOLLOWING_PLAYER() {
        @Override
        public String toString() {
            return "Following Player";
        }

        @Override
        public void init(Camera cam, GameWorld world) {
            cam.setRotationAxis(Rotate.X_AXIS);
            cam.setRotate(60);
            cam.setTranslateZ(-160);
        }

        @Override
        public void update(Camera cam, GameWorld world, Entity spottedEntity) {
            double speedX = 0.03;
            double speedY = 0.06;
            cam.setTranslateX(lerp(cam.getTranslateX(), spottedEntity.position().x(), speedX));
            cam.setTranslateY(lerp(cam.getTranslateY(), spottedEntity.position().y() + 200, speedY));
        }
    },

    NEAR_PLAYER() {
        @Override
        public String toString() {
            return "Near Player";
        }

        @Override
        public void init(Camera cam, GameWorld world) {
            cam.setRotationAxis(Rotate.X_AXIS);
            cam.setRotate(80);
        }

        @Override
        public void update(Camera cam, GameWorld world, Entity spottedEntity) {
            double speed = 0.02;
            double x = lerp(cam.getTranslateX(), spottedEntity.position().x(), speed);
            double y = lerp(cam.getTranslateY(), spottedEntity.position().y() + 150, speed);
            cam.setTranslateX(x);
            cam.setTranslateY(y);
            cam.setTranslateZ(-40);
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