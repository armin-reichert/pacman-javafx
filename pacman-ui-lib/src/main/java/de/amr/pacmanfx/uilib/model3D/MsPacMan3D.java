/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class MsPacMan3D extends PacBase3D {

    private MsPacManFemaleParts femaleBodyParts;

    public MsPacMan3D(
        Model3DRepository model3DRepository,
        AnimationManager animationManager,
        Pac msPacMan,
        double size,
        Color headColor, Color eyesColor, Color palateColor,
        Color hairBowColor, Color hairBowPearlsColor, Color boobsColor)
    {
        super(model3DRepository, animationManager, msPacMan, size, headColor, eyesColor, palateColor);
        femaleBodyParts = model3DRepository.createFemaleBodyParts(size, hairBowColor, hairBowPearlsColor, boobsColor);
        getChildren().add(femaleBodyParts);

        dyingAnimation = new ManagedAnimation(animationManager, "Ms_PacMan_Dying") {
            @Override
            protected Animation createAnimation() {
                var spinning = new RotateTransition(Duration.seconds(0.25), MsPacMan3D.this);
                spinning.setAxis(Rotate.Z_AXIS);
                spinning.setFromAngle(0);
                spinning.setToAngle(360);
                spinning.setInterpolator(Interpolator.LINEAR);
                spinning.setCycleCount(4);
                return spinning;
            }
        };

        movementAnimation = new HipSwayingAnimation(animationManager, this);
        setMovementPowerMode(false);
    }

    @Override
    public void destroy() {
        if (femaleBodyParts != null) {
            femaleBodyParts.destroy();
            femaleBodyParts = null;
        }
        super.destroy();
    }

    @Override
    public void setMovementPowerMode(boolean power) {
        ((HipSwayingAnimation) movementAnimation).setPowerMode(power);
    }
}