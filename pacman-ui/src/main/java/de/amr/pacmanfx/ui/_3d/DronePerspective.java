/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.GameContext;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;

import static de.amr.pacmanfx.lib.UsefulFunctions.lerp;

public class DronePerspective implements Perspective {

    public static final int DEFAULT_Z = -200;

    private double speed;
    private int nearestGroundZ;
    private int farestGroundZ;

    public DronePerspective() {
        speed = 0.05;
        nearestGroundZ = -50;
        farestGroundZ = -500;
    }

    public double speed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public void startControlling(PerspectiveCamera camera) {
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setFieldOfView(40); // default: 30
        camera.setRotationAxis(Rotate.X_AXIS);
        camera.setRotate(0);
        camera.setTranslateX(0);
        camera.setTranslateY(0);
        camera.setTranslateZ(DEFAULT_Z);
    }

    @Override
    public void update(PerspectiveCamera camera, GameContext gameContext) {
        gameContext.currentGame().optGameLevel().ifPresent(gameLevel -> {
            double x = lerp(camera.getTranslateX(), gameLevel.pac().x(), speed);
            double y = lerp(camera.getTranslateY(), gameLevel.pac().y(), speed);
            camera.setTranslateX(x);
            camera.setTranslateY(y);
        });
    }

    public void moveUp(PerspectiveCamera camera) {
        changeZ(camera, -currentDeltaZ(camera));
    }

    public void moveDown(PerspectiveCamera camera) {
        changeZ(camera, currentDeltaZ(camera));
    }

    public void moveDefaultHeight(PerspectiveCamera camera) {
        camera.setTranslateZ(DEFAULT_Z);
    }

    private void changeZ(PerspectiveCamera camera, double deltaZ) {
        final double oldZ = camera.getTranslateZ();
        final double newZ = Math.clamp(oldZ + deltaZ, farestGroundZ, nearestGroundZ);
        camera.setTranslateZ(newZ);
    }

    private double currentDeltaZ(PerspectiveCamera camera) {
        final double logZ = Math.log10(Math.abs(camera.getTranslateZ()));
        return 5 + logZ;
    }
}