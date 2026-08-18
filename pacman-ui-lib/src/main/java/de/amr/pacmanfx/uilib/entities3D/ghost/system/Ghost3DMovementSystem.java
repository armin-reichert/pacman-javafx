/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.ghost.system;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DAnimationComp;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DViewComp;

public class Ghost3DMovementSystem {

    public static void update(Ghost ghost) {
        final Ghost3DViewComp view3D = ghost.reqComp(Ghost3DViewComp.class);

        final Vector2f center = ghost.pos().bodyCenter();
        final double height = view3D.root().getBoundsInParent().getDepth();
        final int angle = switch (ghost.worldNavigation().wishDir()) {
            case LEFT  -> 0;
            case UP    -> 90;
            case RIGHT -> 180;
            case DOWN  -> 270;
        };

        final boolean inTeleportingSpace = ghost.worldNavigation().inTeleportingSpace();
        final boolean visible = ghost.isVisible() && !inTeleportingSpace;

        view3D.root().setVisible(visible);
        view3D.root().setTranslateX(center.x());
        view3D.root().setTranslateY(center.y());
        view3D.root().setTranslateZ(-0.5 * height);

        view3D.facingRotate().setAngle(angle);

        if (ghost.worldNavigation().info().tunnelEntered) {
            ghost.reqComp(Ghost3DAnimationComp.class).braking().playFromStart();
        }
    }
}
