/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.RegisteredAnimation;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class MsPacMan3D extends PacBase3D {

    private MsPacManFemaleParts femaleBodyParts;

    public MsPacMan3D(
        PacManModel3DRepository model3DRepository,
        AnimationRegistry animationRegistry,
        Pac msPacMan,
        double size,
        Color headColor, Color eyesColor, Color palateColor,
        Color hairBowColor, Color hairBowPearlsColor, Color boobsColor)
    {
        super(model3DRepository, animationRegistry, msPacMan, size, headColor, eyesColor, palateColor);
        femaleBodyParts = model3DRepository.createFemaleBodyParts(size, hairBowColor, hairBowPearlsColor, boobsColor);
        getChildren().add(femaleBodyParts);

        dyingAnimation = new RegisteredAnimation(animationRegistry, "Ms_PacMan_Dying") {
            @Override
            protected Animation createAnimationFX() {
                var spinning = new RotateTransition(Duration.seconds(0.25), MsPacMan3D.this);
                spinning.setAxis(Rotate.Z_AXIS);
                spinning.setFromAngle(0);
                spinning.setToAngle(360);
                spinning.setInterpolator(Interpolator.LINEAR);
                spinning.setCycleCount(4);
                return spinning;
            }
        };

        movementAnimation = new HipSwayingAnimation(animationRegistry, this);
        setMovementPowerMode(false);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (femaleBodyParts != null) {
            femaleBodyParts.dispose();
            femaleBodyParts = null;
        }
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