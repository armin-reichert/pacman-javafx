package de.amr.pacmanfx.uilib.model3D.pac;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.PacPowerSystem;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.scene.PointLight;
import org.tinylog.Logger;

public class Pac3DAnimationController {

    private final AnimationRegistry animations;

    public Pac3DAnimationController(AnimationRegistry animations) {
        this.animations = animations;
    }

    public void init() {
        stopAllAnimations();
    }

    public void update(GameContext gameContext, Pac3D pac3D) {
        final GameLevel level = gameContext.assertLevel();
        final Pac pac = level.entities().pac();
        final PacPowerSystem pacPowerSystem = gameContext.systems().pacPowerSystem;

        if (pac.state() == Pac.State.WALKING || pac.state() == Pac.State.STUCK) {
            pac3D.powerLight().ifPresent(powerLight -> updatePowerLight(pacPowerSystem, pac, powerLight));

            animations.optAnimation(Pac3D.AnimationID.MOVING).ifPresent(movementAnimation -> {
                movementAnimation.playOrContinue();
                animations.optAnimation(Pac3D.AnimationID.MOVING, Pac3DMovementAnimation.class).ifPresent(movement -> movement.update(pac));
            });

            animations.optAnimation(Pac3D.AnimationID.CHEWING).ifPresent(chewingAnimation -> {
                if (pac.state() == Pac.State.STUCK) {
                    chewingAnimation.stop();
                } else {
                    chewingAnimation.playOrContinue();
                }
            });
        }
        else {
            animations.optAnimation(Pac3D.AnimationID.MOVING).ifPresent(ManagedAnimation::stop);
            animations.optAnimation(Pac3D.AnimationID.CHEWING).ifPresent(ManagedAnimation::stop);
        }
    }

    public void setPowerMode(boolean power) {
        animations.optAnimation(Pac3D.AnimationID.MOVING, Pac3DMovementAnimation.class)
            .ifPresent(movement -> movement.setPowerMode(power));
    }

    /**
     * When empowered, Pac-Man is lighted, light range shrinks with ceasing power.
     */
    private void updatePowerLight(PacPowerSystem pacPowerSystem, Pac pac, PointLight powerLight) {
        if (powerLight == null) return;

        final boolean powerActive = pacPowerSystem.isPowerActive(pac);
        final long powerTicksRemaining = pacPowerSystem.powerTicksRemaining(pac);
        final long powerTicksTotal = pacPowerSystem.powerTicksTotal(pac);

        if (powerActive && pac.visibility().isVisible() && pac.state() != Pac.State.DEAD) {
            powerLight.setLightOn(true);
            final float maxRange = (powerTicksRemaining / (float) powerTicksTotal) * 60 + 30;
            powerLight.setMaxRange(maxRange);
            Logger.debug("Power remaining: {}, light max range: {0.00}", powerTicksRemaining, maxRange);
        } else {
            powerLight.setLightOn(false);
        }
    }

    private void stopAllAnimations() {
        for (Pac3D.AnimationID animationID : Pac3D.AnimationID.values()) {
            animations.optAnimation(animationID).ifPresent(ManagedAnimation::stop);
        }
    }
}
