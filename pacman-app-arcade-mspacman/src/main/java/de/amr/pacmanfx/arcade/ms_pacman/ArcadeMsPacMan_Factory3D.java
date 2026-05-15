/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.config.EntityConfig;
import de.amr.pacmanfx.ui.d3.DefaultFactory3D;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.model3D.pac.Pac3D;
import de.amr.pacmanfx.uilib.model3D.pac.Pac3DFactory;
import de.amr.pacmanfx.uilib.model3D.pac.PacConfig;
import javafx.scene.Group;

import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_Factory3D extends DefaultFactory3D {

    @Override
    public Pac3D createPac3D(Pac pac, PacConfig config, AnimationRegistry animations) {
        return Pac3DFactory.createMsPacMan3D(animations, pac, config);
    }

    @Override
    public Group createLivesCounterShape3D(EntityConfig entityConfig) {
        requireNonNull(entityConfig);

        final PacConfig config = entityConfig.pacConfig()
            .withModifiedSize3D(entityConfig.livesCounter().shapeSize());

        return new Group(
            Pac3DFactory.createPacBody(config, true),
            Pac3DFactory.createFemalePacBodyParts(config)
        );
    }
}
