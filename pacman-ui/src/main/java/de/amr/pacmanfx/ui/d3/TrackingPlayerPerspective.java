/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.model.GameLevel;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.lerp;
import static java.util.Objects.requireNonNull;

public class TrackingPlayerPerspective implements Perspective<GameLevel> {

    private final PerspectiveCamera camera;

    public TrackingPlayerPerspective(PerspectiveCamera camera) {
        this.camera = requireNonNull(camera);
    }

    @Override
    public void startControlling() {
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setFieldOfView(40); // default: 30
        camera.setRotationAxis(Rotate.X_AXIS);
        camera.setRotate(70);
        camera.setTranslateZ(-60);
    }

    @Override
    public void update(GameLevel level) {
        double speedX = 0.03;
        double speedY = 0.06;
        double worldWidth = level.worldMap().numCols() * TS;
        double targetX = Math.clamp(level.pac().x(), 80, worldWidth - 80);
        double targetY = level.pac().y() + 150;
        camera.setTranslateX(lerp(camera.getTranslateX(), targetX, speedX));
        camera.setTranslateY(lerp(camera.getTranslateY(), targetY, speedY));
    }
}
