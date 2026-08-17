/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.bonus.system;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.uilib.entities3D.bonus.comp.Bonus3DViewComp;

public class Bonus3DMovementSystem {

    public static void update(GameEntity bonus) {
        final Bonus3DViewComp view3D = bonus.requireComp(Bonus3DViewComp.class);

        final Vector2f center = bonus.pos().bodyCenter();

        view3D.translate().setX(center.x());
        view3D.translate().setY(center.y());
        view3D.translate().setZ(-WorldMap.HTS);

        view3D.rollingTransform().update(bonus);
    }
}
