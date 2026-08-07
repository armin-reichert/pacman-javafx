/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.ghost.anim;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DViewComp;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class GhostBrakeAnimation3D extends ManagedAnimation {

    private final Ghost ghost;

    public GhostBrakeAnimation3D(Ghost ghost) {
        super("Ghost Braking (%s)".formatted(ghost.name()));
        this.ghost = ghost;
        setFactory(this::createAnimationFX);
    }

    private Animation createAnimationFX() {
        final Ghost3DViewComp view3D = ghost.requireComponent(Ghost3DViewComp.class);

        var rotateTransition = new RotateTransition(Duration.seconds(0.5), view3D.root());
        rotateTransition.setAxis(Rotate.Y_AXIS);
        rotateTransition.setAutoReverse(true);
        rotateTransition.setCycleCount(2);
        rotateTransition.setInterpolator(Interpolator.EASE_OUT);

        return rotateTransition;
    }

    @Override
    public void playFromStart() {
        var rotateTransition = animationFX();
        rotateTransition.stop();
        adjustAngle(rotateTransition);
        rotateTransition.playFromStart();
    }

    @Override
    public void playOrContinue() {
        var rotateTransition = animationFX();
        rotateTransition.stop();
        rotateTransition.play();
    }

    private void adjustAngle(Animation animation) {
        if (animation instanceof RotateTransition rotateTransition) {
            rotateTransition.setByAngle(
                ghost.worldNavigation().moveDir() == Direction.LEFT ? -35 : 35);
        }
    }

    @Override
    public void stop() {
        super.stop();
        final Node root = ghost.requireComponent(Ghost3DViewComp.class).root();
        root.setRotationAxis(Rotate.Y_AXIS);
        root.setRotate(0);
    }
}
