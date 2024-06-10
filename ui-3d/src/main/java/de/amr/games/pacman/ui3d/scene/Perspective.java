/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.scene;

import de.amr.games.pacman.model.actors.Entity;
import javafx.scene.Camera;
import javafx.scene.SubScene;
import javafx.scene.transform.Rotate;
import org.tinylog.Logger;

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
        public void init(SubScene scene) {
            var cam = scene.getCamera();
            cam.setRotationAxis(Rotate.X_AXIS);
            cam.setRotate(0);
            cam.setTranslateX(0);
            cam.setTranslateY(0);
            cam.setTranslateZ(-500);
        }

        @Override
        public void update(SubScene scene, Entity spottedEntity) {
            var cam = scene.getCamera();
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
        public void init(SubScene scene) {
            var cam = scene.getCamera();
            cam.setRotationAxis(Rotate.X_AXIS);
            cam.setRotate(70);
            cam.setTranslateX(111);
            cam.setTranslateY(480);
            cam.setTranslateZ(-120);
        }
    },

    FOLLOWING_PLAYER() {
        @Override
        public String toString() {
            return "Following Player";
        }

        @Override
        public void init(SubScene scene) {
            var cam = scene.getCamera();
            cam.setRotationAxis(Rotate.X_AXIS);
            cam.setRotate(60);
            cam.setTranslateZ(-160);
        }

        @Override
        public void update(SubScene scene, Entity spottedEntity) {
            var cam = scene.getCamera();
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
        public void init(SubScene scene) {
            var cam = scene.getCamera();
            cam.setRotationAxis(Rotate.X_AXIS);
            cam.setRotate(80);
        }

        @Override
        public void update(SubScene scene, Entity spottedEntity) {
            var cam = scene.getCamera();
            double speed = 0.02;
            double x = lerp(cam.getTranslateX(), spottedEntity.position().x(), speed);
            double y = lerp(cam.getTranslateY(), spottedEntity.position().y() + 150, speed);
            cam.setTranslateX(x);
            cam.setTranslateY(y);
            cam.setTranslateZ(-40);
            Logger.debug("Camera x={0.00} y={0.00} sceneWidth={0}, sceneHeight={0} entityX={0.00}, entityY={0.00}",
                    x, y, scene.getWidth(), scene.getHeight(), spottedEntity.posX(), spottedEntity.posY());
        }
    };

    //TODO how to compute these values for arbitrary sized worlds?
    public static int TOTAL_ROTATE = 70;
    public static int TOTAL_TRANSLATE_X = 108;
    public static int TOTAL_TRANSLATE_Y = 480;
    public static int TOTAL_TRANSLATE_Z = -120;

    public static Perspective next(Perspective p) {
        int n = Perspective.values().length;
        return Perspective.values()[p.ordinal() < n - 1 ? p.ordinal() + 1 : 0];
    }

    public static Perspective previous(Perspective p) {
        int n = Perspective.values().length;
        return Perspective.values()[p.ordinal() > 0 ? p.ordinal() - 1 : n - 1];
    }
}