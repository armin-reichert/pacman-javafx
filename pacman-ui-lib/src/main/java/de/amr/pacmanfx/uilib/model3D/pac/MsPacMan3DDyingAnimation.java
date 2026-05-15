/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.pac;

import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

public class MsPacMan3DDyingAnimation extends ManagedAnimation {

    private final Pac3D msPacMan3D;

    public MsPacMan3DDyingAnimation(Pac3D msPacMan3D) {
        super("Ms. Pac-Man Dying");
        this.msPacMan3D = requireNonNull(msPacMan3D);
        setFactory(this::createAnimation);
    }

    private Animation createAnimation() {
        var spinning = new RotateTransition(Duration.seconds(0.25), msPacMan3D);
        spinning.setAxis(Rotate.Z_AXIS);
        spinning.setFromAngle(0);
        spinning.setToAngle(360);
        spinning.setInterpolator(Interpolator.LINEAR);
        spinning.setCycleCount(4);
        return spinning;
    }
}
