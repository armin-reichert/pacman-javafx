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

        public Drone(PerspectiveCamera camera) {
            super(camera);
        }

        @Override
        public String toString() {
            return "Drone";
        }

        @Override
        public void init(GameWorld world) {
            camera.setNearClip(0.1);
            camera.setFarClip(10000.0);
            camera.setFieldOfView(40); // default: 30
            camera.setRotationAxis(Rotate.X_AXIS);
            camera.setRotate(0);
            camera.setTranslateX(0);
            camera.setTranslateY(0);
            camera.setTranslateZ(-HEIGHT);
        }

        @Override
        public void update(GameWorld world, Actor2D focussedActor) {
            var position = focussedActor.position();
            double speed = 0.02;
            double x = lerp(camera.getTranslateX(), position.x(), speed);
            double y = lerp(camera.getTranslateY(), position.y(), speed);
            camera.setTranslateZ(-HEIGHT);
            camera.setTranslateX(x);
            camera.setTranslateY(y);
        }
    };

    public static class Total extends Perspective {

        public Total(PerspectiveCamera camera) {
            super(camera);
        }

        @Override
        public String toString() {
            return "Total";
        }

        @Override
        public void init(GameWorld world) {
            camera.setNearClip(0.1);
            camera.setFarClip(10000.0);
            camera.setFieldOfView(40); // default: 30
        }

        @Override
        public void update(GameWorld world, Actor2D spottedActor) {
            int sizeX = world.map().terrain().numCols() * TS;
            int sizeY = world.map().terrain().numRows() * TS;
            camera.setRotationAxis(Rotate.X_AXIS);
            camera.setRotate(70);
            camera.setTranslateX(sizeX * 0.5);
            camera.setTranslateY(sizeY * 1.5);
            camera.setTranslateZ(-100);
        }
    }

    public static class TrackingPlayer extends Perspective {

        public TrackingPlayer(PerspectiveCamera camera) {
            super(camera);
        }

        @Override
        public String toString() {
            return "Following Player";
        }

        @Override
        public void init(GameWorld world) {
            camera.setNearClip(0.1);
            camera.setFarClip(10000.0);
            camera.setFieldOfView(40); // default: 30
            camera.setRotationAxis(Rotate.X_AXIS);
            camera.setRotate(70);
            camera.setTranslateZ(-60);
        }

        @Override
        public void update(GameWorld world, Actor2D spottedActor) {
            double speedX = 0.03;
            double speedY = 0.06;
            double worldWidth = world.map().terrain().numCols() * TS;
            double targetX = clamp(spottedActor.posX(), 80, worldWidth - 80);
            double targetY = spottedActor.posY() + 150;
            camera.setTranslateX(lerp(camera.getTranslateX(), targetX, speedX));
            camera.setTranslateY(lerp(camera.getTranslateY(), targetY, speedY));
        }
    }

    public static class StalkingPlayer extends Perspective {

        public StalkingPlayer(PerspectiveCamera camera) {
            super(camera);
        }

        @Override
        public String toString() {
            return "Near Player";
        }

        @Override
        public void init(GameWorld world) {
            camera.setNearClip(0.1);
            camera.setFarClip(10000.0);
            camera.setFieldOfView(40); // default: 30
            camera.setRotationAxis(Rotate.X_AXIS);
            camera.setRotate(80);
        }

        @Override
        public void update(GameWorld world, Actor2D spottedActor) {
            double speedX = 0.04;
            double speedY = 0.04;
            double worldWidth = world.map().terrain().numCols() * TS;
            double targetX = clamp(spottedActor.posX(), 40, worldWidth - 40);
            double targetY = spottedActor.position().y() + 100;
            camera.setTranslateX(lerp(camera.getTranslateX(), targetX, speedX));
            camera.setTranslateY(lerp(camera.getTranslateY(), targetY, speedY));
            camera.setTranslateZ(-40);
        }
    };

    Perspective(PerspectiveCamera camera) {
        this.camera = camera;
    }

    public abstract void init(GameWorld world);

    public abstract void update(GameWorld world, Actor2D spottedActor);

    final PerspectiveCamera camera;
}