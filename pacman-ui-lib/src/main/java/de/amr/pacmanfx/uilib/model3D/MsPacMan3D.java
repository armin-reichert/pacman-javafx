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

    private class HipSwayingAnimation extends ManagedAnimation {
        private static final short HIP_ANGLE_FROM = -20;
        private static final short HIP_ANGLE_TO = 20;
        private static final Duration SWING_TIME = Duration.seconds(0.4);
        private static final float POWER_ANGLE_AMPLIFICATION = 1.5f;
        private static final float POWER_RATE = 2;

        public HipSwayingAnimation(AnimationManager animationManager) {
            super(animationManager, "MsPacMan_HipSwaying");
        }

        @Override
        protected Animation createAnimation() {
            var rotateTransition = new RotateTransition(SWING_TIME, MsPacMan3D.this);
            rotateTransition.setAxis(Rotate.Z_AXIS);
            rotateTransition.setCycleCount(Animation.INDEFINITE);
            rotateTransition.setAutoReverse(true);
            rotateTransition.setInterpolator(Interpolator.EASE_BOTH);
            return rotateTransition;
        }

        @Override
        public void stop() {
            var rotateTransition = (RotateTransition) getOrCreateAnimation();
            super.stop();
            setRotationAxis(rotateTransition.getAxis());
            setRotate(0);
        }

        public void setPowerMode(boolean power) {
            var rotateTransition = (RotateTransition) getOrCreateAnimation();
            boolean running = rotateTransition.getStatus() == Animation.Status.RUNNING;
            double amplification = power ? POWER_ANGLE_AMPLIFICATION : 1;
            rotateTransition.stop();
            rotateTransition.setFromAngle(HIP_ANGLE_FROM * amplification);
            rotateTransition.setToAngle(HIP_ANGLE_TO * amplification);
            rotateTransition.setRate(power ? POWER_RATE : 1);
            if (running) {
                rotateTransition.play();
            }
        }
    }

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

        movementAnimation = new HipSwayingAnimation(animationManager);
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