/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d3.entities.levelcounter.comp;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class LevelCounter3DAnimationComp implements GameEntityComponent, Disposable {

    private ManagedAnimation spinningAnimation;

    public LevelCounter3DAnimationComp(LevelCounter3DViewComp view3D, AnimationRegistry registry) {
        createSpinningAnimation(view3D);
        registry.register(LevelCounter3DAnimationID.LEVEL_COUNTER_SPINNING, spinningAnimation);
    }

    public ManagedAnimation spinningAnimation() {
        return spinningAnimation;
    }

    @Override
    public void reset() {}

    @Override
    public void dispose() {
        if (spinningAnimation != null) {
            spinningAnimation.dispose();
            spinningAnimation = null;
        }
    }

    private void createSpinningAnimation(LevelCounter3DViewComp view3D) {
        spinningAnimation = new ManagedAnimation("Level Counter Spinning");

        spinningAnimation.setAnimationFactory(() -> {
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
