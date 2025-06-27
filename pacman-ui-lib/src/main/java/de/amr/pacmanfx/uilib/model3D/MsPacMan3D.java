/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class MsPacMan3D extends PacBase3D {

    private class HipSwayingAnimation extends ManagedAnimation {
        private static final short HIP_ANGLE_FROM = -20;
        private static final short HIP_ANGLE_TO = 20;
        private static final Duration SWING_TIME = Duration.seconds(0.4);

        public HipSwayingAnimation(AnimationManager animationManager) {
            super(animationManager, "MsPacMan_HipSwaying");
        }

        @Override
        protected Animation createAnimation() {
            var rotateTransition = new RotateTransition(SWING_TIME, root);
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
            root.setRotationAxis(rotateTransition.getAxis());
            root.setRotate(0);
        }

        public void setPowerMode(boolean power) {
            var rotateTransition = (RotateTransition) getOrCreateAnimation();
            double amplification = power ? 1.5 : 1;
            double rate = power ? 2 : 1;
            rotateTransition.stop();
            rotateTransition.setFromAngle(HIP_ANGLE_FROM * amplification);
            rotateTransition.setToAngle(HIP_ANGLE_TO * amplification);
            rotateTransition.setRate(rate);
        }
    }

    public MsPacMan3D(AnimationManager animationManager, Pac pac, double size, AssetStorage assets, String ans) {
        super(animationManager, pac, size, assets, ans);

        Group femaleBodyParts = Model3DRepository.get().createFemaleBodyParts(size,
            assets.color(ans + ".pac.color.hairbow"),
            assets.color(ans + ".pac.color.hairbow.pearls"),
            assets.color(ans + ".pac.color.boobs"));

        root.getChildren().add(femaleBodyParts);

        dyingAnimation = new ManagedAnimation(animationManager, "Ms_PacMan_Dying") {
            @Override
            protected Animation createAnimation() {
                var spinning = new RotateTransition(Duration.seconds(0.25), root);
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
    protected void updateMovementAnimation() {}

    @Override
    public void setMovementPowerMode(boolean power) {
        ((HipSwayingAnimation) movementAnimation).setPowerMode(power);
    }
}