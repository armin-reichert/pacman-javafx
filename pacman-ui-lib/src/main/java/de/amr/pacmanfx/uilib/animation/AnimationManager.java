/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import javafx.animation.Animation;
import org.tinylog.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * A central registry for managed animations.
 */
public class AnimationManager {

    private final Set<ManagedAnimation> animations = new HashSet<>();

    void register(ManagedAnimation managedAnimation) {
        requireNonNull(managedAnimation);
        if (animations.contains(managedAnimation)) {
            Logger.warn("Animation '{}' is already registered", managedAnimation.label);
        } else {
            animations.add(managedAnimation);
            Logger.trace("Animation '{}' registered", managedAnimation.label);
        }
    }

    void playAnimation(ManagedAnimation managedAnimation) {
        requireNonNull(managedAnimation);
        Animation animation = managedAnimation.getOrCreateAnimation();
        requireNonNull(animation);
        if (animation.getStatus() != Animation.Status.RUNNING) {
            Logger.trace("Continuing animation '{}'", managedAnimation.label);
            animation.play();
        }
    }

    void playAnimationFromStart(ManagedAnimation managedAnimation) {
        requireNonNull(managedAnimation);
        Animation animation = managedAnimation.getOrCreateAnimation();
        requireNonNull(animation);
        if (animation.getStatus() != Animation.Status.RUNNING) {
            Logger.trace("Playing animation '{}' from start", managedAnimation.label);
            animation.playFromStart();
        }
    }

    void pauseAnimation(ManagedAnimation managedAnimation) {
        requireNonNull(managedAnimation);
        managedAnimation.animation().ifPresent(animation -> {
            try {
                if (animation.getStatus() != Animation.Status.PAUSED) {
                    animation.pause();
                    Logger.debug("Paused animation '{}'", managedAnimation.label());
                }
            } catch (IllegalStateException x) {
                Logger.warn("Could not pause (embedded?) animation '{}'", managedAnimation.label());
            }
        });
    }

    void stopAnimation(ManagedAnimation managedAnimation) {
        requireNonNull(managedAnimation);
        managedAnimation.animation().ifPresent(animation -> {
            try {
                if (animation.getStatus() != Animation.Status.STOPPED) {
                    animation.stop();
                    Logger.debug("Stopped animation '{}'", managedAnimation.label());
                }
            } catch (IllegalStateException x) {
                Logger.warn("Could not stop (embedded?) animation '{}'", managedAnimation.label());
            }
        });
    }

    void disposeAnimation(ManagedAnimation managedAnimation) {
        requireNonNull(managedAnimation);
        stopAnimation(managedAnimation);
        if (managedAnimation.animation != null) {
            managedAnimation.animation.setOnFinished(null);
        }
        Logger.info("Disposed managed animation '{}'", managedAnimation.label);
    }

    // public API

    public void stopAllAnimations() {
        animations.forEach(this::stopAnimation);
    }

    public void disposeAllAnimations() {
        animations.forEach(this::disposeAnimation);
        animations.clear();
        Logger.info("All animations disposed and removed");
    }

    public Set<ManagedAnimation> animations() {
        return Collections.unmodifiableSet(animations);
    }
}