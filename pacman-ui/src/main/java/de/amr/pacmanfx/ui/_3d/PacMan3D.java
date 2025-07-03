/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import de.amr.pacmanfx.uilib.model3D.PacBase3D;
import javafx.animation.*;
import javafx.geometry.Point3D;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.pacmanfx.uilib.Ufx.doAfterSec;
import static de.amr.pacmanfx.uilib.Ufx.now;

public class PacMan3D extends PacBase3D {

    private class HeadBangingAnimation extends ManagedAnimation {
        private static final short BANG_ANGLE_FROM = -10;
        private static final short BANG_ANGLE_TO = 15;
        private static final Duration BANG_TIME = Duration.seconds(0.3);
        private static final float POWER_ANGLE_AMPLIFICATION = 2;
        private static final float POWER_RATE = 2;

        public HeadBangingAnimation(AnimationManager animationManager) {
            super(animationManager, "Pac_Man_Movement");
        }

        @Override
        protected Animation createAnimation() {
            var rotateTransition = new RotateTransition(BANG_TIME, root);
            rotateTransition.setAxis(Rotate.X_AXIS);
            rotateTransition.setCycleCount(Animation.INDEFINITE);
            rotateTransition.setAutoReverse(true);
            rotateTransition.setInterpolator(Interpolator.EASE_BOTH);
            return rotateTransition;
        }

        @Override
        public void stop() {
            super.stop();
            var rotateTransition = (RotateTransition) animation;
            root.setRotationAxis(rotateTransition.getAxis());
            root.setRotate(0);
        }

        public void update() {
            var rotateTransition = (RotateTransition) getOrCreateAnimation();
            if (pac.isStandingStill()) {
                stop();
            } else {
                Point3D axis = pac.moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
                if (!axis.equals(rotateTransition.getAxis())) {
                    stop();
                    rotateTransition.setAxis(axis);
                }
                play(CONTINUE);
            }
        }

        public void setPowerMode(boolean power) {
            var rotateTransition = (RotateTransition) getOrCreateAnimation();
            boolean running = rotateTransition.getStatus() == Animation.Status.RUNNING;
            rotateTransition.stop();
            rotateTransition.setFromAngle(BANG_ANGLE_FROM * POWER_ANGLE_AMPLIFICATION);
            rotateTransition.setToAngle(BANG_ANGLE_TO * POWER_ANGLE_AMPLIFICATION);
            rotateTransition.setRate(power ? POWER_RATE : 1);
            if (running) {
                rotateTransition.play();
            }
        }
    }

    public PacMan3D(Model3DRepository model3DRepository, AnimationManager animationManager, Pac pac, double size, AssetStorage assets, String ans) {
        super(model3DRepository, animationManager, pac, size, assets, ans);

        dyingAnimation = new ManagedAnimation(animationManager, "PacMan_Dying") {
            @Override
            protected Animation createAnimation() {
                Duration duration = Duration.seconds(1.5);
                byte numSpins = 5;

                var spinning = new RotateTransition(duration.divide(numSpins), root);
                spinning.setAxis(Rotate.Z_AXIS);
                spinning.setByAngle(360);
                spinning.setCycleCount(numSpins);
                spinning.setInterpolator(Interpolator.LINEAR);

                var shrinking = new ScaleTransition(duration.multiply(0.5), root);
                shrinking.setToX(0.25);
                shrinking.setToY(0.25);
                shrinking.setToZ(0.02);

                var expanding = new ScaleTransition(duration.multiply(0.5), root);
                expanding.setToX(0.75);
                expanding.setToY(0.75);

                var sinking = new TranslateTransition(duration, root);
                sinking.setToZ(0);

                return new SequentialTransition(
                    now(PacMan3D.this::init), // TODO check this
                    new ParallelTransition(spinning, new SequentialTransition(shrinking, expanding), sinking),
                    doAfterSec(1.0, () -> {
                        root.setVisible(false);
                        root.setScaleX(1.0);
                        root.setScaleY(1.0);
                        root.setScaleZ(1.0);
                    })
                );
            }
        };

        movementAnimation = new HeadBangingAnimation(animationManager);
        setMovementPowerMode(false);
    }

    @Override
    public void updateMovementAnimation() {
        ((HeadBangingAnimation) movementAnimation).update();
    }

    @Override
    public void setMovementPowerMode(boolean power) {
        ((HeadBangingAnimation) movementAnimation).setPowerMode(power);
    }
}