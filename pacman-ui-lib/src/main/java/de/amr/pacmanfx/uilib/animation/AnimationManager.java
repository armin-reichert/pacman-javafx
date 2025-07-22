/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import javafx.animation.Animation;
import org.tinylog.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

/**
 * A central registry for managed animations.
 */
public class AnimationManager {

    private final Map<String, ManagedAnimation> animationMap = new HashMap<>();

    public void register(String label, ManagedAnimation managedAnimation) {
        requireNonNull(label);
        requireNonNull(managedAnimation);
        if (animationMap.containsValue(managedAnimation)) {
            Logger.warn("Animation '{}' is already registered", label);
        } else {
            String id = label + "_" + UUID.randomUUID();
            animationMap.put(id, managedAnimation);
            Logger.trace("Animation '{}' registered with ID '{}'", label, id);
        }
    }

    public void playAnimation(ManagedAnimation managedAnimation) {
        requireNonNull(managedAnimation);
        Animation animation = managedAnimation.getOrCreateAnimation();
        requireNonNull(animation);
        if (animation.getStatus() != Animation.Status.RUNNING) {
            Logger.trace("Continuing animation '{}'", managedAnimation.label);
            animation.play();
        }
    }

    public void playAnimationFromStart(ManagedAnimation managedAnimation) {
        requireNonNull(managedAnimation);
        Animation animation = managedAnimation.getOrCreateAnimation();
        requireNonNull(animation);
        if (animation.getStatus() != Animation.Status.RUNNING) {
            Logger.trace("Playing animation '{}' from start", managedAnimation.label);
            animation.playFromStart();
        }
    }

    public void pauseAnimation(ManagedAnimation managedAnimation) {
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

    public void stopAnimation(ManagedAnimation managedAnimation) {
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

    public void stopAllAnimations() {
        animationMap.values().forEach(this::stopAnimation);
    }

    public void removeAllAnimations() {
        animationMap.clear();
        Logger.info("Animation map cleared");
    }

    public void destroyAnimation(ManagedAnimation managedAnimation) {
        requireNonNull(managedAnimation);
        stopAnimation(managedAnimation);
        if (managedAnimation.animation != null) {
            managedAnimation.animation.setOnFinished(null);
        }
        Logger.info("Destroyed managed animation '{}'", managedAnimation.label);
    }

    public void destroyAllAnimations() {
        animationMap.values().forEach(this::destroyAnimation);
        removeAllAnimations();
    }

    public Map<String, ManagedAnimation> animationMap() {
        return Collections.unmodifiableMap(animationMap);
    }
}