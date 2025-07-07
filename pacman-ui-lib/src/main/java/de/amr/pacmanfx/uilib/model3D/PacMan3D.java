/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.pacmanfx.uilib.Ufx.doNow;
import static de.amr.pacmanfx.uilib.Ufx.pauseSec;

public class PacMan3D extends PacBase3D {

    public PacMan3D(
        Model3DRepository model3DRepository,
        AnimationManager animationManager,
        Pac pac,
        double size,
        Color headColor, Color eyesColor, Color palateColor)
    {
        super(model3DRepository, animationManager, pac, size, headColor, eyesColor, palateColor);

        dyingAnimation = new ManagedAnimation(animationManager, "PacMan_Dying") {
            @Override
            protected Animation createAnimation() {
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
                    doNow(PacMan3D.this::init), // TODO check this
                    new ParallelTransition(spinning, new SequentialTransition(shrinking, expanding), sinking),
                    pauseSec(1.0, () -> {
                        setVisible(false);
                        setScaleX(1.0);
                        setScaleY(1.0);
                        setScaleZ(1.0);
                    })
                );
            }
        };

        movementAnimation = new HeadBangingAnimation(animationManager, PacMan3D.this);
        setMovementPowerMode(false);
    }

    @Override
    public void updateMovementAnimation() {
        ((HeadBangingAnimation) movementAnimation).update(pac);
    }

    @Override
    public void setMovementPowerMode(boolean power) {
        ((HeadBangingAnimation) movementAnimation).setPowerMode(power);
    }
}