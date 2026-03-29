/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D.actor;

import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.Models3D;
import de.amr.pacmanfx.uilib.model3D.animation.HipSwayingAnimation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

public class MsPacMan3D extends Pac3D {

    public MsPacMan3D(AnimationRegistry animationRegistry, Pac msPacMan, PacConfig pacConfig) {
        super(animationRegistry, msPacMan);

        requireNonNull(pacConfig);

        setBody(Models3D.PAC_MAN_MODEL.createPacBody(pacConfig));
        setJaw(Models3D.PAC_MAN_MODEL.createBlindPacBody(pacConfig));

        final Group femaleParts = Models3D.PAC_MAN_MODEL.createFemaleBodyParts(pacConfig);
        getChildren().add(femaleParts);

        dyingAnimation = new ManagedAnimation("Ms_PacMan_Dying");
        dyingAnimation.setFactory(() -> {
            var spinning = new RotateTransition(Duration.seconds(0.25), MsPacMan3D.this);
            spinning.setAxis(Rotate.Z_AXIS);
            spinning.setFromAngle(0);
            spinning.setToAngle(360);
            spinning.setInterpolator(Interpolator.LINEAR);
            spinning.setCycleCount(4);
            return spinning;
        });
        animationRegistry.register("Ms_PacMan_Dying", dyingAnimation);

        movementAnimation = new HipSwayingAnimation(this);
        animationRegistry.register("Ms_PacMan_Movement", movementAnimation);

        setMovementPowerMode(false);
    }

    @Override
    public void setMovementPowerMode(boolean power) {
        if (movementAnimation instanceof HipSwayingAnimation hipSwayingAnimation) {
            hipSwayingAnimation.setPowerMode(power);
        }
    }

    @Override
    public void updateMovementAnimation() {
        if (movementAnimation instanceof HipSwayingAnimation hipSwayingAnimation) {
            hipSwayingAnimation.update(pac);
        }
    }
}