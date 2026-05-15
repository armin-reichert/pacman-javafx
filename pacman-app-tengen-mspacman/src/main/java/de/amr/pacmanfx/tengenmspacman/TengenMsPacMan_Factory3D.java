/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.config.EntityConfig;
import de.amr.pacmanfx.ui.d3.DefaultFactory3D;
import de.amr.pacmanfx.uilib.animation.ManagedAnimationsRegistry;
import de.amr.pacmanfx.uilib.model3D.pac.Pac3D;
import de.amr.pacmanfx.uilib.model3D.pac.Pac3DFactory;
import de.amr.pacmanfx.uilib.model3D.pac.PacConfig;
import javafx.scene.Group;

import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_Factory3D extends DefaultFactory3D {

    @Override
    public Pac3D createPac3D(Pac pac, PacConfig config, ManagedAnimationsRegistry animations) {
        requireNonNull(pac);
        requireNonNull(config);
        requireNonNull(animations);
        return Pac3DFactory.createMsPacMan3D(animations, pac, config);
    }

    @Override
    public Group createLivesCounterShape3D(EntityConfig config) {
        requireNonNull(config);
        final PacConfig pacConfig = config.pacConfig().withModifiedSize3D(config.livesCounter().shapeSize());
        return Pac3DFactory.createMsPacManBody(pacConfig);
    }
}
