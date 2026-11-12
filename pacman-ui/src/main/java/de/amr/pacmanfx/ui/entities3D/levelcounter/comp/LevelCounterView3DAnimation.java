/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.levelcounter.comp;

import de.amr.basics.Disposable;
import de.amr.basics.ui.animation.AnimationRegistry;
import de.amr.basics.ui.animation.ManagedAnimation;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class LevelCounterView3DAnimation implements Disposable {

    private ManagedAnimation spinning;

    public LevelCounterView3DAnimation(LevelCounterView3D view3D, AnimationRegistry registry) {
        createSpinningAnimation(view3D);
        registry.register(LevelCounter3DAnimationID.LEVEL_COUNTER_SPINNING, spinning);
    }

    public ManagedAnimation spinningAnimation() {
        return spinning;
    }

    @Override
    public void dispose() {
        if (spinning != null) {
            spinning.dispose();
            spinning = null;
        }
    }

    private void createSpinningAnimation(LevelCounterView3D view3D) {
        spinning = new ManagedAnimation("Level Counter Spinning");
        spinning.setAnimationFactory(() -> {
            final var cubesAnimation = new ParallelTransition();
            for (int i = 0; i < view3D.root().getChildren().size(); ++i) {
                final Node cube = view3D.root().getChildren().get(i);
                final var spinning = new RotateTransition(Duration.seconds(6), cube);
                spinning.setCycleCount(Animation.INDEFINITE);
                spinning.setInterpolator(Interpolator.LINEAR);
                spinning.setAxis(Rotate.X_AXIS);
                spinning.setByAngle(i % 2 == 0 ? 360 : -360); // alternate spinning direction
                cubesAnimation.getChildren().add(spinning);
            }
            return cubesAnimation;
        });
    }
}
