/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.GameContext;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;

import static de.amr.pacmanfx.lib.UsefulFunctions.lerp;

public class DronePerspective implements Perspective {

    static final int MIN_HEIGHT_OVER_GROUND = 25;
    static final int MAX_HEIGHT_OVER_GROUND = 1500;

    private final IntegerProperty heightOverGround = new SimpleIntegerProperty(200);

    @Override
    public void attach(PerspectiveCamera camera) {
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setFieldOfView(40); // default: 30
        camera.setRotationAxis(Rotate.X_AXIS);
        camera.setRotate(0);
        camera.setTranslateX(0);
        camera.setTranslateY(0);
        camera.translateZProperty().bind(heightOverGround.negate());
    }

    @Override
    public void detach(PerspectiveCamera camera) {
        camera.translateZProperty().unbind();
    }

    @Override
    public void update(PerspectiveCamera camera, GameContext gameContext) {
        gameContext.optGameLevel().ifPresent(gameLevel -> {
            double speed = 0.05;
            double x = lerp(camera.getTranslateX(), gameLevel.pac().x(), speed);
            double y = lerp(camera.getTranslateY(), gameLevel.pac().y(), speed);
            camera.setTranslateX(x);
            camera.setTranslateY(y);
        });
    }

    public void moveUp() {
        changeHeight(currentHeightDelta());
    }

    public void moveDown() {
        changeHeight(-currentHeightDelta());
    }

    private void changeHeight(int delta) {
        heightOverGround.set(Math.clamp(heightOverGround.get() + delta, MIN_HEIGHT_OVER_GROUND, MAX_HEIGHT_OVER_GROUND));
    }

    private int currentHeightDelta() {
        int height = heightOverGround.get();
        if (height > 100) return height / 10;
        return 5;
    }
}