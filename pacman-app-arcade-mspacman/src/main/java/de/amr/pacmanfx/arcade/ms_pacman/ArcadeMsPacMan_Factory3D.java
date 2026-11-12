/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.core.CommonGameSystems;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.ui.entities3D.pac.comp.PacView3D;
import de.amr.pacmanfx.ui.gamescene.view3d.DefaultFactory3D;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.ui.systems.PacView3DSystem;
import de.amr.pacmanfx.uilib.view3d.PacSettings;
import javafx.scene.Group;

import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_Factory3D extends DefaultFactory3D {

    private static class SingletonHolder {
        static final ArcadeMsPacMan_Factory3D SINGLETON = new ArcadeMsPacMan_Factory3D();
    }

    public static ArcadeMsPacMan_Factory3D instance() {
        return SingletonHolder.SINGLETON;
    }

    protected ArcadeMsPacMan_Factory3D() {}

    @Override
    public void createPac3D(CommonGameSystems systems, Pac pac, PacSettings settings) {
        final PacView3DSystem pacView3DSystem = systems.assertSystem(PacView3DSystem.class);
        final PacView3D view3D = pacView3DSystem.createMsPacManView3D(pac3DShapeFactory, pac, settings);
        pac.setComponent(PacView3D.class, view3D);
    }

    @Override
    public Group createLivesCounterShape3D(WorldSettings settings) {
        requireNonNull(settings);
        final PacSettings config = settings.pac().resized(settings.livesCounter().shapeSize());
        return new Group(
            pac3DShapeFactory.createPacBody(config, true),
            pac3DShapeFactory.createFemalePacBodyParts(config)
        );
    }
}
