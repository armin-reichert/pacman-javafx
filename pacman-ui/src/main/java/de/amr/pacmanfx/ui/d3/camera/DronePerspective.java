/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3.camera;

import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.ui.input.Keyboard;
import javafx.scene.PerspectiveCamera;
import javafx.scene.input.KeyCode;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Rotate;

import static de.amr.basics.math.MoreMath.lerp;
import static java.util.Objects.requireNonNull;

public class DronePerspective implements Perspective<GameLevel> {

    public static final float DEFAULT_Z = -200;
    public static final float DEFAULT_LOWEST_Z = -50;
    public static final float DEFAULT_HIGHEST_Z = -500;

    private final PerspectiveCamera camera;
    private float speed;
    private float lowestZ;
    private float highestZ;

    public DronePerspective(PerspectiveCamera camera) {
        this.camera = requireNonNull(camera);
        speed = 0.05f;
        lowestZ = DEFAULT_LOWEST_Z;
        highestZ = DEFAULT_HIGHEST_Z;
    }

    public void setLowestZ(float lowestZ) {
        this.lowestZ = lowestZ;
    }

    public void setHighestZ(float highestZ) {
        this.highestZ = highestZ;
    }

    public double speed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = (float) speed;
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
        double x = lerp(camera.getTranslateX(), level.entities().pac().x(), speed);
        double y = lerp(camera.getTranslateY(), level.entities().pac().y(), speed);
        camera.setTranslateX(x);
        camera.setTranslateY(y);
    }

    public void handleKeyPressed(Keyboard keyboard) {
        if (keyboard.isKeyPressed(KeyCode.MINUS) && keyboard.controlDown()) {
            moveUp();
        }
        else if (keyboard.isKeyPressed(KeyCode.PLUS) && keyboard.controlDown()) {
            moveDown();
        }
        else if (keyboard.isKeyPressed(KeyCode.DIGIT0) || keyboard.isKeyPressed(KeyCode.NUMPAD0)) {
            moveToDefaultHeight();
        }
    }

    public void handleScrollEvent(ScrollEvent e) {
        if (e.getDeltaY() < 0) {
            moveUp();
        } else if (e.getDeltaY() > 0) {
            moveDown();
        }
    }

    public void moveUp() {
        changeCameraZ(-heightSensitiveDelta());
    }

    public void moveDown() {
        changeCameraZ(heightSensitiveDelta());
    }

    public void moveToDefaultHeight() {
        camera.setTranslateZ(DEFAULT_Z);
    }

    private void changeCameraZ(double deltaZ) {
        final double oldZ = camera.getTranslateZ();
        final double newZ = Math.clamp(oldZ + deltaZ, highestZ, lowestZ);
        camera.setTranslateZ(newZ);
    }

    private double heightSensitiveDelta() {
        return 5 + Math.log10(Math.abs(camera.getTranslateZ()));
    }
}