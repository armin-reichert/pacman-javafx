/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import javafx.animation.Animation;
import org.tinylog.Logger;

import java.util.*;

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
            Logger.debug("Animation with label '{}' is already registered", label);
        } else {
            animationMap.put(label + "_" + UUID.randomUUID(), managedAnimation);
        }
    }

    public void stopAnimation(ManagedAnimation managedAnimation) {
        requireNonNull(managedAnimation);
        managedAnimation.animation().ifPresent(animation -> {
            try {
                if (animation.getStatus() == Animation.Status.STOPPED) {
                    Logger.debug("Already stopped: animation ID='{}' ({})", managedAnimation.id(), managedAnimation);
                } else {
                    animation.stop();
                    Logger.debug("Stopped animation ID='{}' ({})", managedAnimation.id(), managedAnimation);
                }
            } catch (IllegalStateException x) {
                Logger.warn("Could not stop (embedded?) animation ID='{}' ({})", managedAnimation.id(), managedAnimation);
            }
        });
    }

    public void stopAllAnimations() {
        animationMap.values().forEach(this::stopAnimation);
    }

    public void clearAnimations() {
        animationMap.clear();
    }

    public void destroy() {
        stopAllAnimations();
        animationMap.values().forEach(ManagedAnimation::destroy);
        clearAnimations();
    }

    public Map<String, ManagedAnimation> animationMap() {
        return animationMap;
    }
}