/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.animation;

import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.ghost.Ghost3D;
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * Rotates the ghost dress back and forth.
 */
public class GhostDressAnimation3D extends ManagedAnimation {

    private static final float FULL_CYCLE_SEC = 0.6f;

    public GhostDressAnimation3D(Ghost3D ghost3D, int angle) {
        super("Ghost Dress Animation (%s)".formatted(ghost3D.ghost().name()));
        setFactory(() -> {
            final var dressRotation = new RotateTransition(Duration.seconds(FULL_CYCLE_SEC / 2), ghost3D.dressGroup());
            dressRotation.setAxis(Rotate.Y_AXIS); // Note: Y axis!
            dressRotation.setFromAngle(-angle);
            dressRotation.setToAngle(angle);
            dressRotation.setCycleCount(Animation.INDEFINITE);
            dressRotation.setAutoReverse(true);
            return dressRotation;
        });
    }
}
