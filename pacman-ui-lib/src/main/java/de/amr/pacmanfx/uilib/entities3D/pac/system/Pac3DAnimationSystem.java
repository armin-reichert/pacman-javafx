/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.pac.system;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.pac.comp.PacState;
import de.amr.pacmanfx.core.entities.pac.comp.PacStateComp;
import de.amr.pacmanfx.core.entities.pac.system.PacStateSystem;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.entities3D.pac.anim.Pac3DMovementAnimation;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.Pac3DAnimationComp;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.Pac3DViewComp;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;

public class Pac3DAnimationSystem {

    public static void stopAll(Pac pac) {
        final Pac3DAnimationComp animation = pac.requireComponent(Pac3DAnimationComp.class);
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

    public static void update(Pac pac, PacStateSystem pacStateSystem) {
        final PacStateComp state = pac.state();
        final Pac3DAnimationComp animation = pac.requireComponent(Pac3DAnimationComp.class);

        final Pac3DMovementAnimation movement = animation.movement();
        if (movement != null) {
            if (state.isMoving()) {
                movement.managedAnimation().playOrContinue();
                movement.update(pac, pacStateSystem);
            }
            else {
                movement.managedAnimation().stop();
            }
        }

        final ManagedAnimation chewing = animation.chewing();
        if (chewing != null) {
            if (state.isMoving()) {
                chewing.playOrContinue();
            } else {
                chewing.stop();
            }
        }
    }

    public static void setPowerMode(Pac pac, boolean power) {
        final Pac3DAnimationComp animation = pac.requireComponent(Pac3DAnimationComp.class);
        final Pac3DMovementAnimation movementAnimation = animation.movement();
        if (movementAnimation != null) {
            movementAnimation.setPowerMode(power);
        }
    }

    /**
     * When empowered, Pac-Man is lighted, light range shrinks with ceasing power.
     */
    public static void updatePowerLight(Pac pac) {
        final PacStateComp state = pac.state();
        final Pac3DViewComp view3D = pac.requireComponent(Pac3DViewComp.class);

        final boolean lighted = state.pacState() != PacState.DEAD;
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

    public static void playDyingAnimation(
        Pac pac,
        Runnable pacDeadSoundEffect,
        Runnable onFinishedCallback) {
        final Pac3DAnimationComp pacAnimation = pac.requireComponent(Pac3DAnimationComp.class);

        final Animation animation = new SequentialTransition(
            Ufx.pauseSecThen(1.5, pacDeadSoundEffect),
            pacAnimation.dying().delegate(),
            Ufx.pauseSec(0.5)
        );
        animation.setOnFinished(_ -> onFinishedCallback.run());

        pacAnimation.chewing().stop();
        pacAnimation.movement().managedAnimation().stop();

        animation.play();
    }
}
