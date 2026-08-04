/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.bonus;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.world.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.world.map.WorldMap;

public class BonusView3DMovementSystem {

    public static void update(GameEntity bonus) {
        final BonusView3DComp view3D = bonus.requireComponent(BonusView3DComp.class);

        final Vector2f center = WorldNavigationSystem.computeCenter(bonus);

        view3D.translate().setX(center.x());
        view3D.translate().setY(center.y());
        view3D.translate().setZ(-WorldMap.HTS);

        view3D.rollingTransform().update(bonus);
    }
}
