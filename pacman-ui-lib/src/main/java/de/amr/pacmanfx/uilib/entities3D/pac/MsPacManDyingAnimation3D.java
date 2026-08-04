/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.pac;

import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class MsPacManDyingAnimation3D extends ManagedAnimation {

    public MsPacManDyingAnimation3D(Pac3DViewComp view3D) {
        super("Ms. Pac-Man Dying");
        setFactory(() -> createAnimation(view3D.root()));
    }

    private Animation createAnimation(Node node) {
        var spinning = new RotateTransition(Duration.seconds(0.25), node);
        spinning.setAxis(Rotate.Z_AXIS);
        spinning.setFromAngle(0);
        spinning.setToAngle(360);
        spinning.setInterpolator(Interpolator.LINEAR);
        spinning.setCycleCount(4);
        return spinning;
    }
}
