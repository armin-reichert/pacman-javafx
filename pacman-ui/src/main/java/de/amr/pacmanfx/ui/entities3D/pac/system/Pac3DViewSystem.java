/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.pac.system;

import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.ui.entities3D.pac.comp.PacView3D;
import de.amr.pacmanfx.uilib.view3d.Pac3DShapeFactory;
import de.amr.pacmanfx.uilib.view3d.PacSettings;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;

public class Pac3DViewSystem {

    public Pac3DViewSystem() {}

    public PacView3D createPacManView3D(Pac3DShapeFactory shapeFactory, Pac pac, PacSettings config) {
        final PacView3D view3D = new PacView3D();
        view3D.setBodyAndJaw(shapeFactory.createPacBody(config, true), shapeFactory.createPacBody(config, false));
        configurePowerLight(view3D, config.colors().headColor().desaturate());
        return view3D;
    }

    public PacView3D createMsPacManView3D(Pac3DShapeFactory shapeFactory, Pac msPacMan, PacSettings config) {
        final PacView3D view3D = new PacView3D();
        view3D.setBodyAndJaw(shapeFactory.createPacBody(config, true), shapeFactory.createPacBody(config, false));
        view3D.bodyGroup().getChildren().add(shapeFactory.createFemalePacBodyParts(config));
        configurePowerLight(view3D, config.colors().headColor().desaturate());
        return view3D;
    }

    private void configurePowerLight(PacView3D view3D, Color color) {
        final PointLight powerLight = view3D.powerLight();
        powerLight.setColor(color);
        powerLight.translateXProperty().bind(view3D.root().translateXProperty());
        powerLight.translateYProperty().bind(view3D.root().translateYProperty());
        powerLight.setTranslateZ(-30);
    }
}
