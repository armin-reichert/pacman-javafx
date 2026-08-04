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
        update(pac3D, level);

        pac3D.root().setScaleX(1.0);
        pac3D.root().setScaleY(1.0);
        pac3D.root().setScaleZ(1.0);
    }

    public static void update(Pac3D pac3D, GameLevel level) {
        requireNonNull(pac3D);
        requireNonNull(level);

        final Pac pac = pac3D.pac();
        final Vector2f center = WorldNavigationSystem.computeCenter(pac);

        pac3D.root().setTranslateX(center.x());
        pac3D.root().setTranslateY(center.y());
        pac3D.root().setTranslateZ(-8); //TODO should depend on size

        if (pac.worldNavigation().moveDir() != null) {
            pac3D.facingRotate().setAngle(switch (pac.worldNavigation().moveDir()) {
                case LEFT -> 0;
                case UP -> 90;
                case RIGHT -> 180;
                case DOWN -> 270;
            });
        }

        final boolean outside = center.x() < WorldMap.HTS || center.x() > WorldMap.TS * level.worldMap().numCols() - WorldMap.HTS;
        pac3D.root().setVisible(pac.visibility().isVisible() && !outside);
    }
}
