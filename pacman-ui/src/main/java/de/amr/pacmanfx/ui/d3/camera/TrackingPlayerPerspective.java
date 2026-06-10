/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3.camera;

import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;

import static de.amr.pacmanfx.core.Globals_Core.TS;
import static de.amr.pacmanfx.core.Globals_Core.lerp;
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
        final Pac pac = level.entities().pac();
        double speedX = 0.03;
        double speedY = 0.06;
        double worldWidth = level.worldMap().numCols() * TS;
        double targetX = Math.clamp(pac.x(), 80, worldWidth - 80);
        double targetY = pac.y() + 150;
        camera.setTranslateX(lerp(camera.getTranslateX(), targetX, speedX));
        camera.setTranslateY(lerp(camera.getTranslateY(), targetY, speedY));
    }
}
