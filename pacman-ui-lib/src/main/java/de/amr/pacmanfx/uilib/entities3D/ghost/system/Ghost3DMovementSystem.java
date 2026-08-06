/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.ghost.system;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DViewComp;
import javafx.scene.Node;

public class Ghost3DMovementSystem {

    public static void update(Ghost ghost) {
        final Ghost3DViewComp view3D = ghost.requireComponent(Ghost3DViewComp.class);
        final Vector2f center = ghost.pos().center(WorldMap.TS);
        final Node root = view3D.root();
        root.setTranslateX(center.x());
        root.setTranslateY(center.y());
        root.setTranslateZ(-0.5 * root.getBoundsInParent().getDepth());

        view3D.facingRotate().setAngle(switch (ghost.worldNavigation().wishDir()) {
            case LEFT  -> 0;
            case UP    -> 90;
            case RIGHT -> 180;
            case DOWN  -> 270;
        });

        //TODO hide if outside world (teleporting)
    }
}
