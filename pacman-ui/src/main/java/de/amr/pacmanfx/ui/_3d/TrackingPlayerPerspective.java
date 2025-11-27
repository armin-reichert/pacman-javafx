/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.GameContext;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.lerp;

public class TrackingPlayerPerspective implements Perspective {

    @Override
    public void attach(PerspectiveCamera camera) {
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setFieldOfView(40); // default: 30
        camera.setRotationAxis(Rotate.X_AXIS);
        camera.setRotate(70);
        camera.setTranslateZ(-60);
    }

    @Override
    public void update(PerspectiveCamera camera, GameContext gameContext) {
        gameContext.currentGame().optGameLevel().ifPresent(gameLevel -> {
            double speedX = 0.03;
            double speedY = 0.06;
            double worldWidth = gameLevel.worldMap().numCols() * TS;
            double targetX = Math.clamp(gameLevel.pac().x(), 80, worldWidth - 80);
            double targetY = gameLevel.pac().y() + 150;
            camera.setTranslateX(lerp(camera.getTranslateX(), targetX, speedX));
            camera.setTranslateY(lerp(camera.getTranslateY(), targetY, speedY));
        });
    }
}
