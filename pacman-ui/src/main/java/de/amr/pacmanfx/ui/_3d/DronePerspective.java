/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.model.GameLevel;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;

import static de.amr.pacmanfx.lib.UsefulFunctions.lerp;
import static java.util.Objects.requireNonNull;

public class DronePerspective implements Perspective<GameLevel> {

    public static final int DEFAULT_Z = -200;

    private final PerspectiveCamera camera;
    private double speed;
    private int nearestGroundZ;
    private int farestGroundZ;

    public DronePerspective(PerspectiveCamera camera) {
        this.camera = requireNonNull(camera);
        speed = 0.05;
        nearestGroundZ = -50;
        farestGroundZ = -500;
    }

    public void setNearestGroundZ(int nearestGroundZ) {
        this.nearestGroundZ = nearestGroundZ;
    }

    public void setFarestGroundZ(int farestGroundZ) {
        this.farestGroundZ = farestGroundZ;
    }

    public double speed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public void startControlling() {
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
    public void update(GameLevel level) {
        double x = lerp(camera.getTranslateX(), level.pac().x(), speed);
        double y = lerp(camera.getTranslateY(), level.pac().y(), speed);
        camera.setTranslateX(x);
        camera.setTranslateY(y);
    }

    public void moveUp() {
        changeZ(-currentDeltaZ());
    }

    public void moveDown() {
        changeZ(currentDeltaZ());
    }

    public void moveDefaultHeight() {
        camera.setTranslateZ(DEFAULT_Z);
    }

    private void changeZ(double deltaZ) {
        final double oldZ = camera.getTranslateZ();
        final double newZ = Math.clamp(oldZ + deltaZ, farestGroundZ, nearestGroundZ);
        camera.setTranslateZ(newZ);
    }

    private double currentDeltaZ() {
        final double logZ = Math.log10(Math.abs(camera.getTranslateZ()));
        return 5 + logZ;
    }
}