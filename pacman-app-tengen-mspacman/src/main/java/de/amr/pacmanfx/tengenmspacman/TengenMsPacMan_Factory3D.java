/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.ui.gamescene.d3.DefaultFactory3D;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.uilib.entities3D.factory.Pac3DFactory;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.PacSettings;
import javafx.scene.Group;

import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_Factory3D extends DefaultFactory3D {

    @Override
    public void createPac3D(Pac pac, PacSettings settings) {
        Pac3DFactory.createMsPacManView3D(pac, settings);
    }

    @Override
    public Group createLivesCounterShape3D(WorldSettings settings) {
        requireNonNull(settings);

        final PacSettings config = settings.pac()
            .resized(settings.livesCounter().shapeSize());

        return new Group(
            Pac3DFactory.createPacBody(config, true),
            Pac3DFactory.createFemalePacBodyParts(config)
        );
    }
}
