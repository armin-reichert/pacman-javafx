/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Entity;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;

import static de.amr.games.pacman.lib.Globals.lerp;

/**
 * Play scene perspectives.
 *
 * @author Armin Reichert
 */
public enum Perspective {

    DRONE {
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
        public void update(GameWorld world, Entity focussedEntity) {
            var position = focussedEntity.position();
            double speed = 0.02;
            double x = lerp(cam.getTranslateX(), position.x(), speed);
            double y = lerp(cam.getTranslateY(), position.y(), speed);
            cam.setTranslateZ(-HEIGHT);
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
        public void init(GameWorld world) {
            //TODO this is crap and doesn't work correctly for non-Arcade maps
            cam.setRotationAxis(Rotate.X_AXIS);
            cam.setRotate(70);
            cam.setTranslateX(111);
            int numRows = world != null ? world.map().terrain().numRows() : GameModel.ARCADE_MAP_SIZE_Y;
            cam.setTranslateY( 8.5 * numRows + 100);
            cam.setTranslateZ(-80);
        }

        @Override
        public void update(GameWorld world, Entity spottedEntity) {
            //init(world);
        }
    },

    FOLLOWING_PLAYER() {
        @Override
        public String toString() {
            return "Following Player";
        }

        @Override
        public void init(GameWorld world) {
            cam.setRotationAxis(Rotate.X_AXIS);
            cam.setRotate(60);
            cam.setTranslateZ(-160);
        }

        @Override
        public void update(GameWorld world, Entity spottedEntity) {
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
        public void init(GameWorld world) {
            cam.setRotationAxis(Rotate.X_AXIS);
            cam.setRotate(80);
        }

        @Override
        public void update(GameWorld world, Entity spottedEntity) {
            double speed = 0.02;
            double x = lerp(cam.getTranslateX(), spottedEntity.position().x(), speed);
            double y = lerp(cam.getTranslateY(), spottedEntity.position().y() + 150, speed);
            cam.setTranslateX(x);
            cam.setTranslateY(y);
            cam.setTranslateZ(-40);
        }
    };

    public Perspective next() {
        int n = Perspective.values().length, ord = ordinal();
        return Perspective.values()[ord < n - 1 ? ord + 1 : 0];
    }

    public Perspective previous() {
        int n = Perspective.values().length, ord = ordinal();
        return Perspective.values()[ord > 0 ? ord - 1 : n - 1];
    }

    Perspective() {
        cam = new PerspectiveCamera(true);
        cam.setNearClip(0.1);
        cam.setFarClip(10000.0);
        cam.setFieldOfView(40); // default: 30
    }

    public abstract void init(GameWorld world);

    public abstract void update(GameWorld world, Entity spottedEntity);

    public PerspectiveCamera getCamera() {
        return cam;
    }

    protected final PerspectiveCamera cam;
}