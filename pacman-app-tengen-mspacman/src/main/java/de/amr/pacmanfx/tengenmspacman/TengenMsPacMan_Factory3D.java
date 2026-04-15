/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.config.EntityConfig;
import de.amr.pacmanfx.ui.d3.DefaultFactory3D;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.model3D.actor.MsPacMan3D;
import de.amr.pacmanfx.uilib.model3D.actor.Pac3D;
import de.amr.pacmanfx.uilib.model3D.actor.PacConfig;
import de.amr.pacmanfx.uilib.model3D.actor.PacManModel3D;
import javafx.scene.Group;

import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_Factory3D extends DefaultFactory3D {

    @Override
    public Pac3D createPac3D(Pac pac, PacConfig pacConfig, AnimationRegistry animations) {
        requireNonNull(pac);
        requireNonNull(pacConfig);
        requireNonNull(animations);
        return new MsPacMan3D(animations, PacManModel3D.instance(), pac, pacConfig);
    }

    @Override
    public Group createLivesCounterShape3D(EntityConfig entityConfig) {
        requireNonNull(entityConfig);
        final PacConfig pacConfig = entityConfig.pacConfig().withModifiedSize3D(entityConfig.livesCounter().shapeSize());
        return PacManModel3D.instance().createMsPacManBody(pacConfig);
    }
}
