package de.amr.pacmanfx.uilib.entities3D.pac;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.pac.PacPowerSystem;
import de.amr.pacmanfx.core.ecs.systems.pac.PacStateSystem;
import de.amr.pacmanfx.core.model.entities.pac.PacState;
import de.amr.pacmanfx.core.model.entities.pac.PacStateComp;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.scene.PointLight;
import org.tinylog.Logger;

public class Pac3DAnimationSystem {

    public static void init(GameEntity pac) {
        final Pac3DAnimationComp animationComp = pac.requireComponent(Pac3DAnimationComp.class);
        for (Pac3DAnimationID animationID : Pac3DAnimationID.values()) {
            animationComp.animationRegistry().optAnimation(animationID).ifPresent(ManagedAnimation::stop);
        }
    }

    public static void update(GameEntity pac, PacStateSystem pacStateSystem) {
        final PacStateComp state = pac.requireComponent(PacStateComp.class);
        final Pac3DAnimationComp animationComp = pac.requireComponent(Pac3DAnimationComp.class);
        final AnimationRegistry animationRegistry = animationComp.animationRegistry();

        final boolean walking = state.pacState() == PacState.ACTIVE && pacStateSystem.notBlocked(pac);
        if (walking) {
            animationRegistry.optAnimation(Pac3DAnimationID.MOVING, Pac3DMovementAnimation.class).ifPresent(walkingAnimation -> {
                walkingAnimation.playOrContinue();
                walkingAnimation.update(pac, pacStateSystem);
            });
            animationRegistry.optAnimation(Pac3DAnimationID.CHEWING).ifPresent(ManagedAnimation::playOrContinue);
        }
        else {
            animationRegistry.optAnimation(Pac3DAnimationID.MOVING).ifPresent(ManagedAnimation::stop);
            animationRegistry.optAnimation(Pac3DAnimationID.CHEWING).ifPresent(ManagedAnimation::stop);
        }
    }

    public static void setPowerMode(GameEntity pac3D, boolean power) {
        final Pac3DAnimationComp animationComp = pac3D.requireComponent(Pac3DAnimationComp.class);
        final AnimationRegistry animationRegistry = animationComp.animationRegistry();

        animationRegistry.optAnimation(Pac3DAnimationID.MOVING, Pac3DMovementAnimation.class)
            .ifPresent(movement -> movement.setPowerMode(power));
    }

    /**
     * When empowered, Pac-Man is lighted, light range shrinks with ceasing power.
     */
    public static void updatePowerLight(PacPowerSystem pacPowerSystem, GameEntity pac) {
        final PacStateComp state = pac.requireComponent(PacStateComp.class);
        final Pac3DViewComp view3D = pac.requireComponent(Pac3DViewComp.class);

        final boolean lighted = state.pacState() != PacState.DEAD;
        if (lighted) {
            updatePowerLight(pacPowerSystem, pac, view3D.powerLight());
        }
    }

    private static void updatePowerLight(PacPowerSystem pacPowerSystem, GameEntity pac, PointLight powerLight) {
        final PacStateComp state = pac.requireComponent(PacStateComp.class);

        final boolean powerActive      = pacPowerSystem.isPowerActive(pac);
        final long powerTicksRemaining = pacPowerSystem.powerTicksRemaining(pac);
        final long powerTicksTotal     = pacPowerSystem.powerTicksTotal(pac);

        if (powerActive && pac.visibility().isVisible() && state.pacState() != PacState.DEAD) {
            powerLight.setLightOn(true);
            final float maxRange = (powerTicksRemaining / (float) powerTicksTotal) * 60 + 30;
            powerLight.setMaxRange(maxRange);
            Logger.debug("Power remaining: {}, light max range: {0.00}", powerTicksRemaining, maxRange);
        } else {
            powerLight.setLightOn(false);
        }
    }
}
