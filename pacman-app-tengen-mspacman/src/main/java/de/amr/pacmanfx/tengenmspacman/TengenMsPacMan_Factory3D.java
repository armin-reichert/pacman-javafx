/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.ui.GameSystems3D;
import de.amr.pacmanfx.ui.entities3D.pac.comp.PacView3D;
import de.amr.pacmanfx.ui.gamescene.d3.DefaultFactory3D;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.uilib.view3d.PacSettings;
import javafx.scene.Group;

import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_Factory3D extends DefaultFactory3D {

    @Override
    public void createPac3D(Pac pac, PacSettings settings) {
        final GameSystems3D.PacSystems3D pacSystems3D = GameSystems3D.reqSystem(GameSystems3D.PacSystems3D.class);
        final PacView3D view3D = pacSystems3D.view3DSystem().createMsPacManView3D(pac3DShapeFactory, pac, settings);
        pac.setComponent(PacView3D.class, view3D);
    }

    @Override
    public Group createLivesCounterShape3D(WorldSettings settings) {
        requireNonNull(settings);

        final PacSettings config = settings.pac()
            .resized(settings.livesCounter().shapeSize());

        return new Group(
            pac3DShapeFactory.createPacBody(config, true),
            pac3DShapeFactory.createFemalePacBodyParts(config)
        );
    }
}
