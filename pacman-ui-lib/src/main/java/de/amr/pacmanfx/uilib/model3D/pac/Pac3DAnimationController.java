package de.amr.pacmanfx.uilib.model3D.pac;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import javafx.scene.PointLight;
import org.tinylog.Logger;

public class Pac3DAnimationController {

    private final AnimationRegistry animations;

    public Pac3DAnimationController(AnimationRegistry animations) {
        this.animations = animations;
    }

    public void init(Pac3D pac3D) {
        stopAllAnimations();
    }

    public void update(Pac3D pac3D) {
        final Pac pac = pac3D.pac();
        if (pac.isAlive()) {
            pac3D.powerLight().ifPresent(powerLight -> updatePowerLight(pac, powerLight));

            animations.optAnimation(Pac3D.AnimationID.MOVING).ifPresent(movementAnimation -> {
                movementAnimation.playOrContinue();
                animations.optAnimation(Pac3D.AnimationID.MOVING, Pac3DMovementAnimation.class).ifPresent(movement -> movement.update(pac));
            });
            animations.optAnimation(Pac3D.AnimationID.CHEWING).ifPresent(chewingAnimation -> {
                if (pac.isParalyzed()) {
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
    private void updatePowerLight(Pac pac, PointLight powerLight) {
        if (powerLight == null) return;
        final TickTimer powerTimer = pac.powerTimer();
        if (powerTimer.isRunning() && pac.isVisible() && !pac.isDead()) {
            powerLight.setLightOn(true);
            final long remainingTicks = powerTimer.remainingTicks();
            final float maxRange = (remainingTicks / (float) powerTimer.durationTicks()) * 60 + 30;
            powerLight.setMaxRange(maxRange);
            Logger.debug("Power remaining: {}, light max range: {0.00}", remainingTicks, maxRange);
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
