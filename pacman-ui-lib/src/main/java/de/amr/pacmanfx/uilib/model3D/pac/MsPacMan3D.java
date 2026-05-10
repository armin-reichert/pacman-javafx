/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D.pac;

import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.animation.ManagedAnimationsRegistry;
import de.amr.pacmanfx.uilib.model3D.PacManWorld3D;
import de.amr.pacmanfx.uilib.model3D.animation.HipSwayingAnimation3D;
import de.amr.pacmanfx.uilib.model3D.animation.PacChewingAnimation3D;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

public class MsPacMan3D extends Pac3D {

    public MsPacMan3D(ManagedAnimationsRegistry animations, Pac msPacMan, PacConfig pacConfig) {
        super(animations, msPacMan);

        requireNonNull(pacConfig);

        setBody(PacManWorld3D.instance().createPacBody(pacConfig));
        setJaw(PacManWorld3D.instance().createBlindPacBody(pacConfig));

        final Group femaleParts = PacManWorld3D.instance().createFemalePacBodyParts(pacConfig);
        getChildren().add(femaleParts);

        this.animations.register(AnimationID.PAC_CHEWING, new PacChewingAnimation3D(this));

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

        final var movementAnimation = new HipSwayingAnimation3D(this);
        animations.register(AnimationID.PAC_MOVING, movementAnimation);

        setMovementAnimationPowerMode(false);
    }

    @Override
    public void updateMovementAnimation() {
        animations.optAnimation(AnimationID.PAC_MOVING, HipSwayingAnimation3D.class).ifPresent(hsa -> hsa.update(pac));
    }

    @Override
    public void setMovementAnimationPowerMode(boolean power) {
        animations.optAnimation(AnimationID.PAC_MOVING, HipSwayingAnimation3D.class).ifPresent(hsa -> hsa.setPowerMode(power));
    }
}