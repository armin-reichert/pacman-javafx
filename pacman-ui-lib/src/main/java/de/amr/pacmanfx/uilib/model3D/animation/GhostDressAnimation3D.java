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

public class GhostDressAnimation3D extends ManagedAnimation {

    public GhostDressAnimation3D(Ghost3D ghost3D) {
        super("Ghost Dress Animation (%s)".formatted(ghost3D.ghost().name()));
        setFactory(() -> {
            final var animation = new RotateTransition(Duration.seconds(0.3), ghost3D.dressGroup());
            animation.setAxis(Rotate.Y_AXIS); // Note: Y axis!
            animation.setByAngle(30);
            animation.setCycleCount(Animation.INDEFINITE);
            animation.setAutoReverse(true);
            return animation;
        });
    }
}
