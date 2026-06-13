/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.config.WorldConfig;
import de.amr.pacmanfx.ui.gamescene.d3.DefaultFactory3D;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.model3D.pac.Pac3D;
import de.amr.pacmanfx.uilib.model3D.pac.Pac3DFactory;
import de.amr.pacmanfx.uilib.model3D.pac.PacConfig;
import javafx.scene.Group;

import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_Factory3D extends DefaultFactory3D {

    @Override
    public Pac3D createPac3D(Pac pac, PacConfig config, AnimationRegistry animationRegistry) {
        return Pac3DFactory.createMsPacMan3D(animationRegistry, pac, config);
    }

    @Override
    public Group createLivesCounterShape3D(WorldConfig worldConfig) {
        requireNonNull(worldConfig);

        final PacConfig config = worldConfig.pacConfig()
            .withModifiedSize3D(worldConfig.livesCounter().shapeSize());

        return new Group(
            Pac3DFactory.createPacBody(config, true),
            Pac3DFactory.createFemalePacBodyParts(config)
        );
    }
}
