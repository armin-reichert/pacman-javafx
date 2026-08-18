/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.ghost.anim;

import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DViewComp;
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * Rotates the ghost dress back and forth.
 */
public class GhostDressAnimation3D extends ManagedAnimation {

    private static final float DRESS_MOVEMENT_ANGLE = 30;

    private static final float FULL_CYCLE_SEC = 0.8f;

    public GhostDressAnimation3D(Ghost ghost) {
        super("Ghost Dress Animation (%s)".formatted(ghost.name()));
        setAnimationFactory(() -> {
            final Ghost3DViewComp view3D = ghost.reqComp(Ghost3DViewComp.class);
            final var dressRotation = new RotateTransition(Duration.seconds(FULL_CYCLE_SEC / 2), view3D.dressGroup());
            dressRotation.setAxis(Rotate.Y_AXIS); // TODO: Check which Y axis
            dressRotation.setFromAngle(-DRESS_MOVEMENT_ANGLE);
            dressRotation.setToAngle(DRESS_MOVEMENT_ANGLE);
            dressRotation.setCycleCount(Animation.INDEFINITE);
            dressRotation.setAutoReverse(true);
            return dressRotation;
        });
    }
}
