/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.pac.system;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.pac.PacPowerSystem;
import de.amr.pacmanfx.core.ecs.systems.pac.PacStateSystem;
import de.amr.pacmanfx.core.model.entities.pac.PacState;
import de.amr.pacmanfx.core.model.entities.pac.PacStateComp;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.entities3D.pac.anim.Pac3DAnimationID;
import de.amr.pacmanfx.uilib.entities3D.pac.anim.Pac3DMovementAnimation;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.Pac3DAnimationComp;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.Pac3DViewComp;

public class Pac3DAnimationSystem {

    public static void init(GameEntity pac) {
        final Pac3DAnimationComp animationComp = pac.requireComponent(Pac3DAnimationComp.class);
        for (Pac3DAnimationID animationID : Pac3DAnimationID.values()) {
            animationComp.animationRegistry().optAnimation(animationID).ifPresent(ManagedAnimation::stop);
        }
    }

    public static void update(GameEntity pac, PacStateSystem pacStateSystem) {
        final PacStateComp state = pac.requireComponent(PacStateComp.class);
        final Pac3DAnimationComp animation = pac.requireComponent(Pac3DAnimationComp.class);

        final boolean walking = state.pacState() == PacState.ACTIVE && pacStateSystem.notBlocked(pac);

        final Pac3DMovementAnimation movement = animation.movementAnimation();
        if (movement != null) {
            if (walking) {
                movement.managedAnimation().playOrContinue();
                movement.update(pac, pacStateSystem);
            }
            else {
                movement.managedAnimation().stop();
            }
        }

        final ManagedAnimation chewing = animation.chewingAnimation();
        if (chewing != null) {
            if (walking) {
                chewing.playOrContinue();
            } else {
                chewing.stop();
            }
        }
    }

    public static void setPowerMode(GameEntity pac, boolean power) {
        final Pac3DAnimationComp animation = pac.requireComponent(Pac3DAnimationComp.class);
        final Pac3DMovementAnimation movementAnimation = animation.movementAnimation();
        if (movementAnimation != null) {
            movementAnimation.setPowerMode(power);
        }
    }

    public static void playDyingAnimation(GameEntity pac) {
        //TODO
    }

    /**
     * When empowered, Pac-Man is lighted, light range shrinks with ceasing power.
     */
    public static void updatePowerLight(GameEntity pac, PacPowerSystem pacPowerSystem) {
        final PacStateComp state = pac.requireComponent(PacStateComp.class);
        final Pac3DViewComp view3D = pac.requireComponent(Pac3DViewComp.class);
        final boolean lighted = state.pacState() != PacState.DEAD;
        if (lighted) {
            final boolean powerActive      = pacPowerSystem.isPowerActive(pac);
            final long powerTicksRemaining = pacPowerSystem.powerTicksRemaining(pac);
            final long powerTicksTotal     = pacPowerSystem.powerTicksTotal(pac);

            if (powerActive && pac.visibility().isVisible() && state.pacState() != PacState.DEAD) {
                view3D.powerLight().setLightOn(true);
                final float maxRange = (powerTicksRemaining / (float) powerTicksTotal) * 60 + 30;
                view3D.powerLight().setMaxRange(maxRange);
            } else {
                view3D.powerLight().setLightOn(false);
            }
        }
    }
}
