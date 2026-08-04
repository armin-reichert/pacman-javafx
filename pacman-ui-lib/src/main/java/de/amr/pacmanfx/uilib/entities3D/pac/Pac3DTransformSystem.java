/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.pac;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.ecs.systems.world.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.entities.pac.Pac;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.WorldMap;

import static java.util.Objects.requireNonNull;

public class Pac3DTransformSystem {

    public static void init(Pac3D pac3D, GameLevel level) {
        final Pac3DViewComp view3D = pac3D.requireComponent(Pac3DViewComp.class);

        update(pac3D, level);

        view3D.root().setScaleX(1.0);
        view3D.root().setScaleY(1.0);
        view3D.root().setScaleZ(1.0);
    }

    public static void update(Pac3D pac3D, GameLevel level) {
        requireNonNull(pac3D);
        requireNonNull(level);

        final Pac3DViewComp view3D = pac3D.requireComponent(Pac3DViewComp.class);

        final Pac pac = pac3D.pac();
        final Vector2f center = WorldNavigationSystem.computeCenter(pac);

        view3D.root().setTranslateX(center.x());
        view3D.root().setTranslateY(center.y());
        view3D.root().setTranslateZ(-8); //TODO should depend on size

        if (pac.worldNavigation().moveDir() != null) {
            view3D.facingRotate().setAngle(switch (pac.worldNavigation().moveDir()) {
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
