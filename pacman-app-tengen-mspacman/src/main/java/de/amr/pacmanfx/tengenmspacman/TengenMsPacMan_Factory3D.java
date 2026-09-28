/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.ui.gamescene.d3.DefaultFactory3D;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.uilib.view3d.PacSettings;
import javafx.scene.Group;

import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_Factory3D extends DefaultFactory3D {

    @Override
    public void createPac3D(Pac pac, PacSettings settings) {
        viewFactory.createMsPacManView3D(shapeFactory, pac, settings);
    }

    @Override
    public Group createLivesCounterShape3D(WorldSettings settings) {
        requireNonNull(settings);

        final PacSettings config = settings.pac()
            .resized(settings.livesCounter().shapeSize());

        return new Group(
            shapeFactory.createPacBody(config, true),
            shapeFactory.createFemalePacBodyParts(config)
        );
    }
}
