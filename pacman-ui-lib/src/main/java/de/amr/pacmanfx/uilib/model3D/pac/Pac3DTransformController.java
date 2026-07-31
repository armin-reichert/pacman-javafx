/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.pac;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.world.WorldMap;

public class Pac3DTransformController {

    public void init(Pac3D pac3D, GameContext gameContext) {
        update(gameContext, pac3D);
        pac3D.root().setTranslateZ(-8); //TODO
        pac3D.root().setScaleX(1.0);
        pac3D.root().setScaleY(1.0);
        pac3D.root().setScaleZ(1.0);
    }

    public void update(GameContext gameContext, Pac3D pac3D) {
        final Pac pac = pac3D.pac();
        final Vector2f center = WorldNavigationSystem.computeCenter(pac);

        pac3D.root().setTranslateX(center.x());
        pac3D.root().setTranslateY(center.y());

        pac3D.facingRotate().setAngle(switch (pac.worldNavigation().moveDir()) {
            case LEFT  -> 0;
            case UP    -> 90;
            case RIGHT -> 180;
            case DOWN  -> 270;
        });

        gameContext.optLevel().ifPresent(level -> {
            final boolean outside = center.x() < WorldMap.HTS
                || center.x() > WorldMap.TS * level.worldMap().numCols() - WorldMap.HTS;
            pac3D.root().setVisible(pac.visibility().isVisible() && !outside);
        });
    }
}
