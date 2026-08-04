/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.entities3D.animation;

import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.entities3D.pac.Pac3D;
import javafx.animation.*;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.basics.util.Ufx.doNow;
import static de.amr.basics.util.Ufx.pauseSecThen;
import static java.util.Objects.requireNonNull;

public class PacManDyingAnimation3D extends ManagedAnimation {

    private final Pac3D pacMan3D;

    public PacManDyingAnimation3D(Pac3D pacMan3D) {
        super("PacMan_Dying");
        this.pacMan3D = requireNonNull(pacMan3D);
        setFactory(this::createAnimation);
    }

    private Animation createAnimation() {
        final Duration duration = Duration.seconds(1.5);
        byte numSpins = 5;

        final var spinning = new RotateTransition(duration.divide(numSpins), pacMan3D.root());
        spinning.setAxis(Rotate.Z_AXIS);
        spinning.setByAngle(360);
        spinning.setCycleCount(numSpins);
        spinning.setInterpolator(Interpolator.LINEAR);

        final var shrinking = new ScaleTransition(duration.multiply(0.5), pacMan3D.root());
        shrinking.setToX(0.25);
        shrinking.setToY(0.25);
        shrinking.setToZ(0.02);

        final var expanding = new ScaleTransition(duration.multiply(0.5), pacMan3D.root());
        expanding.setToX(0.75);
        expanding.setToY(0.75);

        final var sinking = new TranslateTransition(duration, pacMan3D.root());
        sinking.setToZ(0);

        final var deathFight = new ParallelTransition(spinning, new SequentialTransition(shrinking, expanding), sinking);
        return new SequentialTransition(
            doNow(() -> pacMan3D.powerLight().ifPresent(powerLight -> powerLight.setLightOn(false))),
            deathFight,
            pauseSecThen(1.0, () -> {
                pacMan3D.root().setVisible(false);
                //TODO check this:
                pacMan3D.root().setScaleX(1.0);
                pacMan3D.root().setScaleY(1.0);
                pacMan3D.root().setScaleZ(1.0);
            })
        );
    }
}
