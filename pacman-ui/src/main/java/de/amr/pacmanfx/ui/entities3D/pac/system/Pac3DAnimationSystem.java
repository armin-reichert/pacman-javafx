/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.pac.system;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.actor.pac.PacState;
import de.amr.pacmanfx.core.entities.actor.pac.PacStateComp;
import de.amr.pacmanfx.ui.entities3D.pac.anim.Pac3DMovementAnimation;
import de.amr.pacmanfx.ui.entities3D.pac.comp.Pac3DAnimationComp;
import de.amr.pacmanfx.ui.entities3D.pac.comp.PacView3D;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;

public class Pac3DAnimationSystem {

    public void stopAnimations(Pac pac) {
        final Pac3DAnimationComp animation = pac.assertComponent(Pac3DAnimationComp.class);
        if (animation.chewing() != null) {
            animation.chewing().stop();
        }
        if (animation.dying() != null) {
            animation.dying().stop();
        }
        if (animation.movement() != null) {
            animation.movement().managedAnimation().stop();
        }
    }

    public void updateAnimations(Pac pac) {
        final Pac3DAnimationComp animation = pac.assertComponent(Pac3DAnimationComp.class);

        final Pac3DMovementAnimation movementAnimation = animation.movement();
        if (movementAnimation != null) {
            movementAnimation.update();
        }

        if (animation.chewing() != null) {
            final boolean moved = pac.worldNavigation().info().moved;
            if (moved) {
                animation.chewing().playOrContinue();
            } else {
                animation.chewing().stop();
            }
        }

        updatePowerLight(pac);
    }

    public void setPowerMode(Pac pac, boolean power) {
        final Pac3DAnimationComp animation = pac.assertComponent(Pac3DAnimationComp.class);
        final Pac3DMovementAnimation movementAnimation = animation.movement();
        if (movementAnimation != null) {
            movementAnimation.setPowerMode(power);
        }
    }

    public void playDyingAnimation(
        Pac pac,
        Runnable pacDeadSoundEffect,
        Runnable onFinishedCallback) {
        final Pac3DAnimationComp pacAnimation = pac.assertComponent(Pac3DAnimationComp.class);

        final Animation animation = new SequentialTransition(
            Ufx.pauseSecThen(1.5, pacDeadSoundEffect),
            pacAnimation.dying().delegate(),
            Ufx.pauseSec(0.5)
        );
        animation.setOnFinished(_ -> onFinishedCallback.run());

        if (pacAnimation.chewing() != null) {
            pacAnimation.chewing().stop();
        }
        if (pacAnimation.movement() != null) {
            pacAnimation.movement().managedAnimation().stop();
        }
        animation.play();
    }

    /**
     * When empowered, Pac-Man is lighted, light range shrinks with ceasing power.
     */
    private void updatePowerLight(Pac pac) {
        final PacStateComp state = pac.state();
        final PacView3D view3D = pac.assertComponent(PacView3D.class);

        final boolean lighted = state.enumValue() != PacState.DEAD;
        if (lighted) {
            final boolean powerActive      = pac.power().isActive();
            final long powerTicksRemaining = pac.power().ticksRemaining();
            final long powerTicksTotal     = pac.power().ticksTotal();
            if (powerActive && pac.isVisible()) {
                view3D.powerLight().setLightOn(true);
                final float maxRange = (powerTicksRemaining / (float) powerTicksTotal) * 60 + 30;
                view3D.powerLight().setMaxRange(maxRange);
            } else {
                view3D.powerLight().setLightOn(false);
            }
        }
    }
}
