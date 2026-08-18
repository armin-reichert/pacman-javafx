/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.entities3D.pac.anim;

import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.Pac3DViewComp;
import javafx.animation.*;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class PacChewingAnimation3D extends ManagedAnimation {

    private static final Point3D AXIS = Rotate.Y_AXIS;
    private final Pac pac;

    public PacChewingAnimation3D(Pac pac) {
        super("Pac-Man Chewing");
        this.pac = pac;

        final Pac3DViewComp view3D = pac.reqComp(Pac3DViewComp.class);
        final Node jaw = view3D.jaw();
        setAnimationFactory(() -> {
            final var mouthClosed = new KeyValue[]{
                new KeyValue(jaw.rotationAxisProperty(), AXIS),
                new KeyValue(jaw.rotateProperty(), -54, Interpolator.LINEAR)
            };
            final var mouthOpen = new KeyValue[]{
                new KeyValue(jaw.rotationAxisProperty(), AXIS),
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
            return chewing;
        });
    }
}
