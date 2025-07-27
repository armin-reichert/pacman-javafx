/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
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

    static final int MIN_HEIGHT_OVER_GROUND = 50;
    static final int MAX_HEIGHT_OVER_GROUND = 2000;

    private final IntegerProperty heightOfGroundProperty = new SimpleIntegerProperty(200);

    @Override
    public void init(PerspectiveCamera camera) {
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setFieldOfView(40); // default: 30
        camera.setRotationAxis(Rotate.X_AXIS);
        camera.setRotate(0);
        camera.setTranslateX(0);
        camera.setTranslateY(0);
        camera.translateZProperty().bind(heightOfGroundProperty.negate());
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
        int height = heightOfGroundProperty.get();
        int delta = 5;
        if (height > 100) {
            delta = height / 10;
        }
        if (height + delta <= MAX_HEIGHT_OVER_GROUND) {
            heightOfGroundProperty.set(height + delta);
        }
    }

    public void moveDown() {
        int height = heightOfGroundProperty.get();
        int delta = -5;
        if (height > 100) {
            delta = -height / 10;
        }
        if (height + delta >= MIN_HEIGHT_OVER_GROUND) {
            heightOfGroundProperty.set(height + delta);
        }
    }
}
