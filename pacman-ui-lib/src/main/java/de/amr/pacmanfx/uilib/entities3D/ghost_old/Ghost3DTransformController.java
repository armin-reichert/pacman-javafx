/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.ghost_old;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.WorldMap;

public class Ghost3DTransformController {

    public Ghost3DTransformController() {}

    public void init(Ghost3DWrapperToBeRemoved ghost3D, GameContext game) {
        update(ghost3D, game);
    }

    public void update(Ghost3DWrapperToBeRemoved ghost3D, GameContext game) {
        final Ghost ghost = ghost3D.ghost();
        final Vector2f center = WorldNavigationSystem.computeCenter(ghost);

        ghost3D.root().setTranslateX(center.x());
        ghost3D.root().setTranslateY(center.y());
        ghost3D.root().setTranslateZ(-0.5 * ghost3D.root().getBoundsInParent().getDepth());

        ghost3D.facingRotate().setAngle(switch (ghost.worldNavigation().wishDir()) {
            case LEFT  -> 0;
            case UP    -> 90;
            case RIGHT -> 180;
            case DOWN  -> 270;
        });

        final GameLevel level = game.session().assertLevel();
        final boolean outside = center.x() < WorldMap.HTS
            || center.x() > WorldMap.TS * level.worldMap().numCols() - WorldMap.HTS;
        ghost3D.root().setVisible(ghost.isVisible() && !outside);
    }
}
