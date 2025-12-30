/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.Disposable;
import org.tinylog.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * A registry for managed animations.
 */
public class AnimationRegistry implements Disposable {

    private final Set<RegisteredAnimation> registeredAnimations = new HashSet<>();
    private final Set<RegisteredAnimation> markedForDisposal = new HashSet<>();

    void register(RegisteredAnimation registeredAnimation) {
        requireNonNull(registeredAnimation);
        if (registeredAnimations.contains(registeredAnimation)) {
            Logger.warn("Animation '{}' is already registered", registeredAnimation.label());
        } else {
            registeredAnimations.add(registeredAnimation);
            Logger.trace("Animation '{}' registered", registeredAnimation.label());
        }
    }

    void markForDisposal(RegisteredAnimation registeredAnimation) {
        if (markedForDisposal.contains(registeredAnimation)) {
            return;
        }
        if (!registeredAnimations.contains(registeredAnimation)) {
            Logger.error("Animation '{}' is not registered, cannot be marked for disposal", registeredAnimation.label());
            return;
        }
        registeredAnimations.remove(registeredAnimation);
        markedForDisposal.add(registeredAnimation);
    }

    @Override
    public void dispose() {
        Logger.info("Dispose {} animations", markedForDisposal.size());
        markedForDisposal.forEach(RegisteredAnimation::dispose);
        markedForDisposal.clear();
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
        return Collections.unmodifiableSet(markedForDisposal);
    }

    public Set<RegisteredAnimation> animations() {
        return Collections.unmodifiableSet(registeredAnimations);
    }
}