/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D.animation;

import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.actor.PacMan3D;
import javafx.animation.*;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.pacmanfx.uilib.animation.AnimationSupport.doNow;
import static de.amr.pacmanfx.uilib.animation.AnimationSupport.pauseSecThen;
import static java.util.Objects.requireNonNull;

public class PacMan3DDyingAnimation extends ManagedAnimation {

    private final PacMan3D pacMan3D;

    public PacMan3DDyingAnimation(PacMan3D pacMan3D) {
        super("PacMan_Dying");
        this.pacMan3D = requireNonNull(pacMan3D);
        setFactory(this::createAnimation);
    }

    private Animation createAnimation() {
        final Duration duration = Duration.seconds(1.5);
        byte numSpins = 5;

        final var spinning = new RotateTransition(duration.divide(numSpins), pacMan3D);
        spinning.setAxis(Rotate.Z_AXIS);
        spinning.setByAngle(360);
        spinning.setCycleCount(numSpins);
        spinning.setInterpolator(Interpolator.LINEAR);

        final var shrinking = new ScaleTransition(duration.multiply(0.5), pacMan3D);
        shrinking.setToX(0.25);
        shrinking.setToY(0.25);
        shrinking.setToZ(0.02);

        final var expanding = new ScaleTransition(duration.multiply(0.5), pacMan3D);
        expanding.setToX(0.75);
        expanding.setToY(0.75);

        final var sinking = new TranslateTransition(duration, pacMan3D);
        sinking.setToZ(0);

        final var deathFight = new ParallelTransition(spinning, new SequentialTransition(shrinking, expanding), sinking);
        return new SequentialTransition(
            doNow(pacMan3D::updatePowerLight),
            deathFight,
            pauseSecThen(1.0, () -> {
                pacMan3D.setVisible(false);
                //TODO check this:
                pacMan3D.setScaleX(1.0);
                pacMan3D.setScaleY(1.0);
                pacMan3D.setScaleZ(1.0);
            })
        );
    }
}
