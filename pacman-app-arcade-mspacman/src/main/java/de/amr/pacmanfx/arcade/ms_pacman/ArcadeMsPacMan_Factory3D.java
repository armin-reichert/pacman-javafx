/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.ui.entities3D.pac.comp.PacView3D;
import de.amr.pacmanfx.ui.gamescene.view3d.DefaultFactory3D;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.ui.systems.PacSystems3D;
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
    public void createPac3D(GameSystems systems, Pac pac, PacSettings settings) {
        final PacSystems3D pacSystems3D = systems.assertSystem(PacSystems3D.class);
        final PacView3D view3D = pacSystems3D.view3DSystem().createMsPacManView3D(pac3DShapeFactory, pac, settings);
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
