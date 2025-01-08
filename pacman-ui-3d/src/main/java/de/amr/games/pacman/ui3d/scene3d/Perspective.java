/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.scene3d;

import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Actor2D;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * Play scene perspectives.
 *
 * @author Armin Reichert
 */
public abstract class Perspective {

    public enum Name {
        DRONE, TOTAL, TRACK_PLAYER, NEAR_PLAYER;

        public Name prev() {
            int n = values().length, ord = ordinal();
            return values()[ord == 0 ? n - 1 : ord - 1];
        }

        public Name next() {
            int n = values().length, ord = ordinal();
            return values()[ord < n - 1 ? ord + 1 : 0];
        }
    }

    public static class Drone extends Perspective {
        static final int HEIGHT = 200;

        @Override
        public String toString() {
            return "Drone";
        }

        @Override
        public void init(GameWorld world) {
            cam.setRotationAxis(Rotate.X_AXIS);
            cam.setRotate(0);
            cam.setTranslateX(0);
            cam.setTranslateY(0);
            cam.setTranslateZ(-HEIGHT);
        }

        @Override
        public void update(GameWorld world, Actor2D focussedActor) {
            var position = focussedActor.position();
            double speed = 0.02;
            double x = lerp(cam.getTranslateX(), position.x(), speed);
            double y = lerp(cam.getTranslateY(), position.y(), speed);
            cam.setTranslateZ(-HEIGHT);
            cam.setTranslateX(x);
            cam.setTranslateY(y);
        }
    };

    public static class Total extends Perspective {
        @Override
        public String toString() {
            return "Total";
        }

        @Override
        public void init(GameWorld world) {
        }

        @Override
        public void update(GameWorld world, Actor2D spottedActor) {
            int sizeX = world.map().terrain().numCols() * TS;
            int sizeY = world.map().terrain().numRows() * TS;
            cam.setRotationAxis(Rotate.X_AXIS);
            cam.setRotate(70);
            cam.setTranslateX(sizeX * 0.5);
            cam.setTranslateY(sizeY * 1.5);
            cam.setTranslateZ(-100);
        }
    }

    public static class TrackingPlayer extends Perspective {
        @Override
        public String toString() {
            return "Following Player";
        }

        @Override
        public void init(GameWorld world) {
            cam.setRotationAxis(Rotate.X_AXIS);
            cam.setRotate(60);
            cam.setTranslateZ(-120);
        }

        @Override
        public void update(GameWorld world, Actor2D spottedActor) {
            double speedX = 0.03;
            double speedY = 0.06;
            double worldWidth = world.map().terrain().numCols() * TS;
            double targetX = clamp(spottedActor.posX(), 100, worldWidth - 100);
            double targetY = spottedActor.posY() + 150;
            cam.setTranslateX(lerp(cam.getTranslateX(), targetX, speedX));
            cam.setTranslateY(lerp(cam.getTranslateY(), targetY, speedY));
        }
    }

    public static class StalkingPlayer extends Perspective {
        @Override
        public String toString() {
            return "Near Player";
        }

        @Override
        public void init(GameWorld world) {
            cam.setRotationAxis(Rotate.X_AXIS);
            cam.setRotate(80);
        }

        @Override
        public void update(GameWorld world, Actor2D spottedActor) {
            double speedX = 0.04;
            double speedY = 0.04;
            double worldWidth = world.map().terrain().numCols() * TS;
            double targetX = clamp(spottedActor.posX(), 40, worldWidth - 40);
            double targetY = spottedActor.position().y() + 100;
            cam.setTranslateX(lerp(cam.getTranslateX(), targetX, speedX));
            cam.setTranslateY(lerp(cam.getTranslateY(), targetY, speedY));
            cam.setTranslateZ(-40);
        }
    };

    Perspective() {
        cam = new PerspectiveCamera(true);
        cam.setNearClip(0.1);
        cam.setFarClip(10000.0);
        cam.setFieldOfView(40); // default: 30
    }

    public abstract void init(GameWorld world);

    public abstract void update(GameWorld world, Actor2D spottedActor);

    public PerspectiveCamera getCamera() {
        return cam;
    }

    protected final PerspectiveCamera cam;
}