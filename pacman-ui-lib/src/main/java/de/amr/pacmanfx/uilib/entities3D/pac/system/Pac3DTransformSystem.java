/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.pac.system;

import de.amr.basics.math.Vector3f;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.WorldNavigationComp;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.entities.pac.PacState;
import de.amr.pacmanfx.core.model.entities.pac.comp.PacStateComp;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.Pac3DViewComp;

import static java.util.Objects.requireNonNull;

public class Pac3DTransformSystem {

    public static void init(GameEntity pac, GameLevel level) {
        final Pac3DViewComp view3D = pac.requireComponent(Pac3DViewComp.class);

        view3D.root().setScaleX(1.0);
        view3D.root().setScaleY(1.0);
        view3D.root().setScaleZ(1.0);

        update(pac, level);
    }

    public static void update(GameEntity pac, GameLevel level) {
        requireNonNull(pac);
        requireNonNull(level);

        final PacStateComp state = pac.requireComponent(PacStateComp.class);
        final Pac3DViewComp view3D = pac.requireComponent(Pac3DViewComp.class);
        final WorldNavigationComp worldNavigation = pac.requireComponent(WorldNavigationComp.class);

        if (state.pacState() == PacState.ACTIVE) {
            final Vector3f center = new Vector3f(WorldNavigationSystem.computeCenter(pac), -8);
            view3D.root().setTranslateX(center.x());
            view3D.root().setTranslateY(center.y());
            view3D.root().setTranslateZ(center.z()); //TODO should depend on size

            if (worldNavigation.moveDir() != null) {
                view3D.facingRotate().setAngle(switch (worldNavigation.moveDir()) {
                    case LEFT -> 0;
                    case UP -> 90;
                    case RIGHT -> 180;
                    case DOWN -> 270;
                });
            }

            final boolean outside = center.x() < WorldMap.HTS || center.x() > WorldMap.TS * level.worldMap().numCols() - WorldMap.HTS;
            view3D.root().setVisible(pac.visibility().isVisible() && !outside);
        }
    }
}
