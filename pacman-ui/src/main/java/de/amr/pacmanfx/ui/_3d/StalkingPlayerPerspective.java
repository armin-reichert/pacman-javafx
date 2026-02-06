/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.model.GameLevel;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.lerp;
import static java.util.Objects.requireNonNull;

public class StalkingPlayerPerspective implements Perspective<GameLevel> {

    private final PerspectiveCamera camera;

    public StalkingPlayerPerspective(PerspectiveCamera camera) {
        this.camera = requireNonNull(camera);
    }

    @Override
    public void startControlling() {
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setFieldOfView(40); // default: 30
        camera.setRotationAxis(Rotate.X_AXIS);
        camera.setRotate(80);
    }

    @Override
    public void update(GameLevel level) {
        double speedX = 0.04;
        double speedY = 0.04;
        double worldWidth = level.worldMap().numCols() * TS;
        double targetX = Math.clamp(level.pac().x(), 40, worldWidth - 40);
        double targetY = level.pac().y() + 100;
        camera.setTranslateX(lerp(camera.getTranslateX(), targetX, speedX));
        camera.setTranslateY(lerp(camera.getTranslateY(), targetY, speedY));
        camera.setTranslateZ(-40);
    }
}
