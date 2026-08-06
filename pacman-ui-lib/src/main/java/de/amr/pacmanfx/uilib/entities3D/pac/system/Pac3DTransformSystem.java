/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.pac.system;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.pac.comp.PacState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.Pac3DViewComp;

import static java.util.Objects.requireNonNull;

public class Pac3DTransformSystem {

    public static void init(Pac pac, GameLevel level) {
        final Pac3DViewComp view3D = pac.requireComponent(Pac3DViewComp.class);

        view3D.root().setScaleX(1.0);
        view3D.root().setScaleY(1.0);
        view3D.root().setScaleZ(1.0);

        update(pac, level);
    }

    public static void update(Pac pac, GameLevel level) {
        requireNonNull(pac);
        requireNonNull(level);

        final Pac3DViewComp view3D = pac.requireComponent(Pac3DViewComp.class);
        final Vector2f center = WorldNavigationSystem.computeCenter(pac);

        if (pac.state().pacState() == PacState.ACTIVE) {
            updateVisibility(pac, center, level.worldMap());
            updatePosition(view3D, center);
            final Direction moveDir = pac.worldNavigation().moveDir();
            if (moveDir != null) {
                updateFacing(view3D, moveDir);
            }
        }
    }

    private static void updateVisibility(Pac pac, Vector2f center, WorldMap worldMap) {
        final Pac3DViewComp view3D = pac.requireComponent(Pac3DViewComp.class);
        final boolean outside = center.x() < WorldMap.HTS
            || center.x() > WorldMap.TS * worldMap.numCols() - WorldMap.HTS;
        view3D.root().setVisible(pac.isVisible() && !outside);
    }

    private static void updatePosition(Pac3DViewComp view3D, Vector2f center) {
        view3D.root().setTranslateX(center.x());
        view3D.root().setTranslateY(center.y());
        view3D.root().setTranslateZ(-8); //TODO should depend on size
    }
    
    private static void updateFacing(Pac3DViewComp view3D, Direction dir) {
        final int angle = switch (dir) {
            case LEFT -> 0;
            case UP -> 90;
            case RIGHT -> 180;
            case DOWN -> 270;
        };
        view3D.facingRotate().setAngle(angle);
    }
}
