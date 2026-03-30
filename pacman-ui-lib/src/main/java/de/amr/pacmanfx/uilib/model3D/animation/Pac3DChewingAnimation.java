/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D.animation;

import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.actor.Pac3D;
import javafx.animation.*;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

public class Pac3DChewingAnimation extends ManagedAnimation {

    private final Pac3D pac3D;

    public Pac3DChewingAnimation(Pac3D pac3D) {
        super("Pac-Man Chewing");
        this.pac3D = requireNonNull(pac3D);
        setFactory(this::createChewingAnimation);
    }

    private Animation createChewingAnimation() {
        final Group jaw = pac3D.jaw();
        final var mouthClosed = new KeyValue[]{
            new KeyValue(jaw.rotationAxisProperty(), Rotate.Y_AXIS),
            new KeyValue(jaw.rotateProperty(), -54, Interpolator.LINEAR)
        };
        final var mouthOpen = new KeyValue[]{
            new KeyValue(jaw.rotationAxisProperty(), Rotate.Y_AXIS),
            new KeyValue(jaw.rotateProperty(), 0, Interpolator.LINEAR)
        };
        final var chewing = new Timeline(
            new KeyFrame(Duration.ZERO, "Open on Start", mouthOpen),
            new KeyFrame(Duration.millis(100), "Start Closing", mouthOpen),
            new KeyFrame(Duration.millis(130), "Closed", mouthClosed),
            new KeyFrame(Duration.millis(200), "Start Opening", mouthClosed),
            new KeyFrame(Duration.millis(280), "Open", mouthOpen)
        );
        chewing.setCycleCount(Animation.INDEFINITE);
        chewing.statusProperty().addListener((_, _, newStatus) -> {
            if (newStatus == Animation.Status.STOPPED) {
                jaw.setRotationAxis(Rotate.Y_AXIS);
                jaw.setRotate(0);
            }
        });
        return chewing;
    }
}
