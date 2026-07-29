/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.ghost;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.world.WorldMap;

public class Ghost3DTransformController {

    public Ghost3DTransformController() {}

    public void init(Ghost3D ghost3D, GameContext gameContext) {
        update(ghost3D, gameContext);
    }

    public void update(Ghost3D ghost3D, GameContext gameContext) {
        final Ghost ghost = ghost3D.ghost();
        final Vector2f center = WorldNavigationSystem.computeCenter(ghost);

        ghost3D.setTranslateX(center.x());
        ghost3D.setTranslateY(center.y());
        ghost3D.setTranslateZ(-0.5 * ghost3D.getBoundsInParent().getDepth());

        ghost3D.facingRotate().setAngle(switch (ghost.worldNavigation().wishDir()) {
            case LEFT  -> 0;
            case UP    -> 90;
            case RIGHT -> 180;
            case DOWN  -> 270;
        });

        gameContext.optLevel().ifPresent(level -> {
            final boolean outside = center.x() < WorldMap.HTS
                || center.x() > WorldMap.TS * level.worldMap().numCols() - WorldMap.HTS;
            ghost3D.setVisible(ghost.visibility().isVisible() && !outside);
        });
    }
}
