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

    private final Set<RegisteredAnimation> registeredAnimations = new HashSet<>();
    private final Set<RegisteredAnimation> disposedAnimations = new HashSet<>();

    void register(RegisteredAnimation registeredAnimation) {
        requireNonNull(registeredAnimation);
        if (registeredAnimations.contains(registeredAnimation)) {
            Logger.warn("Animation '{}' is already registered", registeredAnimation.label());
        } else {
            registeredAnimations.add(registeredAnimation);
            Logger.trace("Animation '{}' registered", registeredAnimation.label());
        }
    }

    void markDisposed(RegisteredAnimation registeredAnimation) {
        if (disposedAnimations.contains(registeredAnimation)) {
            Logger.warn("Animation '{}' has already been disposed", registeredAnimation.label());
            return;
        }
        if (!registeredAnimations.contains(registeredAnimation)) {
            Logger.error("Animation '{}' is not registered, cannot be marked as disposed", registeredAnimation.label());
            return;
        }
        registeredAnimations.remove(registeredAnimation);
        disposedAnimations.add(registeredAnimation);
    }

    public void clear() {
        Logger.info("Clearing {} disposed animations", disposedAnimations.size());
        disposedAnimations.clear();
    }

    public void stopAllAnimations() {
        registeredAnimations.forEach(RegisteredAnimation::stop);
    }

    public void disposeAllAnimations() {
        registeredAnimations.forEach(RegisteredAnimation::dispose);
        registeredAnimations.clear();
        Logger.info("All animations disposed and removed");
    }

    public Set<RegisteredAnimation> disposedAnimations() {
        return Collections.unmodifiableSet(disposedAnimations);
    }

    public Set<RegisteredAnimation> animations() {
        return Collections.unmodifiableSet(registeredAnimations);
    }
}