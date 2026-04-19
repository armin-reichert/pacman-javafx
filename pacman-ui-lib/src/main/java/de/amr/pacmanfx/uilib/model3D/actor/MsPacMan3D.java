/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D.actor;

import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.ManagedAnimationsRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.animation.HipSwayingAnimation;
import de.amr.pacmanfx.uilib.model3D.animation.Pac3DChewingAnimation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

public class MsPacMan3D extends Pac3D {

    public MsPacMan3D(ManagedAnimationsRegistry animations, PacManModel3D model3D, Pac msPacMan, PacConfig pacConfig) {
        super(animations, msPacMan);

        requireNonNull(pacConfig);

        setBody(model3D.createPacBody(pacConfig));
        setJaw(model3D.createBlindPacBody(pacConfig));

        final Group femaleParts = model3D.createFemaleBodyParts(pacConfig);
        getChildren().add(femaleParts);

        this.animations.register(AnimationID.PAC_CHEWING, new Pac3DChewingAnimation(this));

        final var dyingAnimation = new ManagedAnimation("Ms. Pac-Man Dying");
        dyingAnimation.setFactory(() -> {
            var spinning = new RotateTransition(Duration.seconds(0.25), MsPacMan3D.this);
            spinning.setAxis(Rotate.Z_AXIS);
            spinning.setFromAngle(0);
            spinning.setToAngle(360);
            spinning.setInterpolator(Interpolator.LINEAR);
            spinning.setCycleCount(4);
            return spinning;
        });
        animations.register(AnimationID.PAC_DYING, dyingAnimation);

        final var movementAnimation = new HipSwayingAnimation(this);
        animations.register(AnimationID.PAC_MOVING, movementAnimation);

        setMovementAnimationPowerMode(false);
    }

    @Override
    public void updateMovementAnimation() {
        animations.optAnimation(AnimationID.PAC_MOVING, HipSwayingAnimation.class).ifPresent(hsa -> hsa.update(pac));
    }

    @Override
    public void setMovementAnimationPowerMode(boolean power) {
        animations.optAnimation(AnimationID.PAC_MOVING, HipSwayingAnimation.class).ifPresent(hsa -> hsa.setPowerMode(power));
    }
}