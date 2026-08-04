/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d3.entities.levelcounter;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.entities3D.DisposableGraphicsObject;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class LevelCounterView3DComp implements GameEntityComponent, DisposableGraphicsObject {

    private final Group root = new Group();
    private ManagedAnimation spinningAnimation;

    public LevelCounterView3DComp() {
        spinningAnimation = createSpinningAnimation();
    }

    public Group root() {
        return root;
    }

    public ManagedAnimation spinningAnimation() {
        return spinningAnimation;
    }

    @Override
    public void reset() {
    }

    @Override
    public void dispose() {
        if (spinningAnimation != null) {
            spinningAnimation.dispose();
            spinningAnimation = null;
        }
        cleanupGroup(root, true);
    }

    private ManagedAnimation createSpinningAnimation() {
        spinningAnimation = new ManagedAnimation("Level Counter Spinning");

        spinningAnimation.setFactory(() -> {
            final var cubesAnimation = new ParallelTransition();
            for (int i = 0; i < root.getChildren().size(); ++i) {
                final Node cube = root.getChildren().get(i);
                final var spinning = new RotateTransition(Duration.seconds(6), cube);
                spinning.setCycleCount(Animation.INDEFINITE);
                spinning.setInterpolator(Interpolator.LINEAR);
                spinning.setAxis(Rotate.X_AXIS);
                spinning.setByAngle(i % 2 == 0 ? 360 : -360); // alternate spinning direction
                cubesAnimation.getChildren().add(spinning);
            }
            return cubesAnimation;
        });

        return spinningAnimation;
    }
}
