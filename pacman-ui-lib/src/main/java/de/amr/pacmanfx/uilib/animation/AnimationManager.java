/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import javafx.animation.Animation;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static de.amr.pacmanfx.Validations.requireValidIdentifier;
import static java.util.Objects.requireNonNull;

/**
 * A central registry for managed animations.
 */
public class AnimationManager {

    private Map<String, ManagedAnimation> animationMap = new HashMap<>();

    private String makeID(String description) {
        return description + "#" + UUID.randomUUID();
    }

    public String register(String identifier, ManagedAnimation managedAnimation) {
        requireValidIdentifier(identifier);
        requireNonNull(managedAnimation);
        String id = makeID(identifier);
        animationMap.put(id, managedAnimation);
        return id;
    }

    public void stopAnimation(ManagedAnimation managedAnimation) {
        requireNonNull(managedAnimation);
        managedAnimation.animation().ifPresent(animation -> {
            try {
                if (animation.getStatus() == Animation.Status.STOPPED) {
                    Logger.debug("Already stopped: animation ID='{}' ({})", managedAnimation.identifier(), managedAnimation);
                } else {
                    animation.stop();
                    Logger.debug("Stopped animation ID='{}' ({})", managedAnimation.identifier(), managedAnimation);
                }
            } catch (IllegalStateException x) {
                Logger.warn("Could not stop (embedded?) animation ID='{}' ({})", managedAnimation.identifier(), managedAnimation);
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
        if (animationMap != null) {
            stopAllAnimations();
            clearAnimations();
            animationMap = null;
        }
    }

    public Map<String, ManagedAnimation> animationMap() {
        return animationMap;
    }
}