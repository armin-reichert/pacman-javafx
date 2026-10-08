/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.bonus.system;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.ui.entities3D.bonus.comp.BonusView3D;

import static de.amr.basics.TileDimension.HTS;

class Bonus3DMovementSystem {

    public void update(GameEntity bonus) {
        final BonusView3D view3D = bonus.assertComponent(BonusView3D.class);

        final Vector2f center = bonus.pos().bodyCenter();

        view3D.translate().setX(center.x());
        view3D.translate().setY(center.y());
        view3D.translate().setZ(-HTS);

        view3D.rollingTransform().update(bonus);
    }
}
