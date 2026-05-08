/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.actor;

import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class DressAnimation extends ManagedAnimation {

    public DressAnimation(Ghost ghost, Node dress) {
        super("Ghost Dress Animation (%s)".formatted(ghost.name()));
        setFactory(() -> {
            final var animation = new RotateTransition(Duration.seconds(0.3), dress);
            // TODO: I expected this should be the z-axis but...
            animation.setAxis(Rotate.Y_AXIS);
            animation.setByAngle(30);
            animation.setCycleCount(Animation.INDEFINITE);
            animation.setAutoReverse(true);
            return animation;
        });
    }
}
