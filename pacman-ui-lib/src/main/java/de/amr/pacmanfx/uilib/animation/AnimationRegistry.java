/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import org.tinylog.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * A registry for managed animations.
 */
public class AnimationRegistry {

    private final Set<ManagedAnimation> registeredAnimations = new HashSet<>();

    void register(ManagedAnimation managedAnimation) {
        requireNonNull(managedAnimation);
        if (registeredAnimations.contains(managedAnimation)) {
            Logger.warn("Animation '{}' is already registered", managedAnimation.label());
        } else {
            registeredAnimations.add(managedAnimation);
            Logger.trace("Animation '{}' registered", managedAnimation.label());
        }
    }

    public void stopAllAnimations() {
        registeredAnimations.forEach(ManagedAnimation::stop);
    }

    public void disposeAllAnimations() {
        registeredAnimations.forEach(ManagedAnimation::dispose);
        registeredAnimations.clear();
        Logger.info("All animations disposed and removed");
    }

    public Set<ManagedAnimation> animations() {
        return Collections.unmodifiableSet(registeredAnimations);
    }
}