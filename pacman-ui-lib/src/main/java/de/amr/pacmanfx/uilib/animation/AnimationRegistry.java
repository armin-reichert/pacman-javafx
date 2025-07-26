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
    private final Set<ManagedAnimation> disposedAnimations = new HashSet<>();

    void register(ManagedAnimation managedAnimation) {
        requireNonNull(managedAnimation);
        if (registeredAnimations.contains(managedAnimation)) {
            Logger.warn("Animation '{}' is already registered", managedAnimation.label());
        } else {
            registeredAnimations.add(managedAnimation);
            Logger.trace("Animation '{}' registered", managedAnimation.label());
        }
    }

    void markDisposed(ManagedAnimation managedAnimation) {
        if (disposedAnimations.contains(managedAnimation)) {
            Logger.warn("Animation '{}' has already been disposed", managedAnimation.label());
            return;
        }
        if (!registeredAnimations.contains(managedAnimation)) {
            Logger.error("Animation '{}' is not registered, cannot be marked as disposed", managedAnimation.label());
            return;
        }
        registeredAnimations.remove(managedAnimation);
        disposedAnimations.add(managedAnimation);
    }

    public void stopAllAnimations() {
        registeredAnimations.forEach(ManagedAnimation::stop);
    }

    public void disposeAllAnimations() {
        registeredAnimations.forEach(ManagedAnimation::dispose);
        registeredAnimations.clear();
        Logger.info("All animations disposed and removed");
    }

    public Set<ManagedAnimation> disposedAnimations() {
        return Collections.unmodifiableSet(disposedAnimations);
    }

    public Set<ManagedAnimation> animations() {
        return Collections.unmodifiableSet(registeredAnimations);
    }
}