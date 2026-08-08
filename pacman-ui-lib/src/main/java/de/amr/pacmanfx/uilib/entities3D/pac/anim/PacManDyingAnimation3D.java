/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.entities3D.pac.anim;

import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.Pac3DViewComp;
import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.basics.util.Ufx.doNow;
import static de.amr.basics.util.Ufx.pauseSecThen;

public class PacManDyingAnimation3D extends ManagedAnimation {

    public PacManDyingAnimation3D(Pac3DViewComp view3D) {
        super("PacMan_Dying");
        setAnimationFactory(() -> createAnimation(view3D.root(), view3D.powerLight()));
    }

    private Animation createAnimation(Node node, PointLight powerLight) {
        final Duration duration = Duration.seconds(1.5);
        byte numSpins = 5;

        final var spinning = new RotateTransition(duration.divide(numSpins), node);
        spinning.setAxis(Rotate.Z_AXIS);
        spinning.setByAngle(360);
        spinning.setCycleCount(numSpins);
        spinning.setInterpolator(Interpolator.LINEAR);

        final var shrinking = new ScaleTransition(duration.multiply(0.5), node);
        shrinking.setToX(0.25);
        shrinking.setToY(0.25);
        shrinking.setToZ(0.02);

        final var expanding = new ScaleTransition(duration.multiply(0.5), node);
        expanding.setToX(0.75);
        expanding.setToY(0.75);

        final var sinking = new TranslateTransition(duration, node);
        sinking.setToZ(0);

        final var deathFight = new ParallelTransition(spinning, new SequentialTransition(shrinking, expanding), sinking);
        return new SequentialTransition(
            doNow(() -> powerLight.setLightOn(false)),
            deathFight,
            pauseSecThen(1.0, () -> {
                node.setVisible(false);
                //TODO check this:
                node.setScaleX(1.0);
                node.setScaleY(1.0);
                node.setScaleZ(1.0);
            })
        );
    }
}
