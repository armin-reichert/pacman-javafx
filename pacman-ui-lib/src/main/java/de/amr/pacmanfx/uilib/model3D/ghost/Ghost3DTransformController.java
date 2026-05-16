/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.ghost;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.world.WorldMap;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;

public class Ghost3DTransformController {

    public Ghost3DTransformController() {}

    public void init(Ghost3D ghost3D, WorldMap worldMap) {
        update(ghost3D, worldMap);
    }

    public void update(Ghost3D ghost3D, WorldMap worldMap) {
        final Ghost ghost = ghost3D.ghost();
        final Vector2f center = ghost.center();

        ghost3D.setTranslateX(center.x());
        ghost3D.setTranslateY(center.y());
        ghost3D.setTranslateZ(-0.5 * ghost3D.getBoundsInParent().getDepth());

        ghost3D.facingRotate().setAngle(switch (ghost.wishDir()) {
            case LEFT  -> 0;
            case UP    -> 90;
            case RIGHT -> 180;
            case DOWN  -> 270;
        });

        final boolean outside = center.x() < HTS || center.x() > TS * worldMap.numCols() - HTS;
        ghost3D.setVisible(ghost.isVisible() && !outside);
    }
}
