/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D.actor;

import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.AnimationSupport;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.Models3D;
import de.amr.pacmanfx.uilib.model3D.animation.HeadBangingAnimation;
import javafx.animation.*;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class PacMan3D extends Pac3D {

    public PacMan3D(AnimationRegistry animationRegistry, Pac pac, PacConfig pacConfig) {
        super(animationRegistry, pac);

        setBody(Models3D.PAC_MAN_MODEL.createPacBody(pacConfig));
        setJaw(Models3D.PAC_MAN_MODEL.createBlindPacBody(pacConfig));

        final var dyingAnimation = new ManagedAnimation("PacMan_Dying");
        dyingAnimation.setFactory(() -> {
            Duration duration = Duration.seconds(1.5);
            byte numSpins = 5;

            var spinning = new RotateTransition(duration.divide(numSpins), PacMan3D.this);
            spinning.setAxis(Rotate.Z_AXIS);
            spinning.setByAngle(360);
            spinning.setCycleCount(numSpins);
            spinning.setInterpolator(Interpolator.LINEAR);

            var shrinking = new ScaleTransition(duration.multiply(0.5), PacMan3D.this);
            shrinking.setToX(0.25);
            shrinking.setToY(0.25);
            shrinking.setToZ(0.02);

            var expanding = new ScaleTransition(duration.multiply(0.5), PacMan3D.this);
            expanding.setToX(0.75);
            expanding.setToY(0.75);

            var sinking = new TranslateTransition(duration, PacMan3D.this);
            sinking.setToZ(0);

            return new SequentialTransition(
                new ParallelTransition(spinning, new SequentialTransition(shrinking, expanding), sinking),
                AnimationSupport.pauseSecThen(1.0, () -> {
                    setVisible(false);
                    setScaleX(1.0);
                    setScaleY(1.0);
                    setScaleZ(1.0);
                })
            );
        });
        animationRegistry.register(AnimationID.PAC_DYING, dyingAnimation);
        animationRegistry.register(AnimationID.PAC_MOVING, new HeadBangingAnimation(PacMan3D.this));

        setMovementAnimationPowerMode(false);
    }

    @Override
    public void updateMovementAnimation() {
        animations.optAnimation(Pac3D.AnimationID.PAC_MOVING, HeadBangingAnimation.class)
            .ifPresent(hba -> hba.update(pac));
    }

    @Override
    public void setMovementAnimationPowerMode(boolean power) {
        animations.optAnimation(Pac3D.AnimationID.PAC_MOVING, HeadBangingAnimation.class)
            .ifPresent(hba -> hba.setPowerMode(power));
    }
}