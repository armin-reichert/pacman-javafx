/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D;

import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.ui.entities3D.pac.comp.Pac3DTransformComp;
import de.amr.pacmanfx.ui.entities3D.pac.comp.Pac3DViewComp;
import de.amr.pacmanfx.uilib.entities3d.Pac3DShapeFactory;
import de.amr.pacmanfx.uilib.entities3d.PacSettings;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;

public class Pac3DViewFactory {

    public void createPacManView3D(Pac3DShapeFactory shapeFactory, Pac pac, PacSettings config) {
        ensurePacHas3DView(pac);
        final Pac3DViewComp view3D = pac.reqComp(Pac3DViewComp.class);
        view3D.setBodyAndJaw(shapeFactory.createPacBody(config, true), shapeFactory.createPacBody(config, false));
        configurePowerLight(view3D, config.colors().headColor().desaturate());
    }

    public void createMsPacManView3D(Pac3DShapeFactory shapeFactory, Pac msPacMan, PacSettings config) {
        ensurePacHas3DView(msPacMan);
        final Pac3DViewComp view3D = msPacMan.reqComp(Pac3DViewComp.class);
        view3D.setBodyAndJaw(shapeFactory.createPacBody(config, true), shapeFactory.createPacBody(config, false));
        view3D.bodyGroup().getChildren().add(shapeFactory.createFemalePacBodyParts(config));
        configurePowerLight(view3D, config.colors().headColor().desaturate());
    }

    private void ensurePacHas3DView(Pac pac) {
        if (!pac.hasComp(Pac3DViewComp.class)) {
            pac.setComp(Pac3DViewComp.class, new Pac3DViewComp());
            pac.setComp(Pac3DTransformComp.class, new Pac3DTransformComp());
        }
    }

    private void configurePowerLight(Pac3DViewComp view3D, Color color) {
        final PointLight powerLight = view3D.powerLight();
        powerLight.setColor(color);
        powerLight.translateXProperty().bind(view3D.root().translateXProperty());
        powerLight.translateYProperty().bind(view3D.root().translateYProperty());
        powerLight.setTranslateZ(-30);
    }
}
