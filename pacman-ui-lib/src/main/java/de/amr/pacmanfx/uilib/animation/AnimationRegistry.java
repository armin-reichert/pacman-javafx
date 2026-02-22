/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
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

    private final Set<ManagedAnimation> registered = new HashSet<>();

    public void register(ManagedAnimation animation) {
        requireNonNull(animation);
        if (registered.contains(animation)) {
            Logger.warn("Animation '{}' is already registered", animation.label());
        } else {
            registered.add(animation);
            Logger.info("Animation '{}' registered", animation.label());
        }
    }

    public void garbageCollect() {
        var disposedAnimations = registered.stream().filter(ManagedAnimation::disposed).toList();
        disposedAnimations.forEach(registered::remove);
        Logger.info("Removed {} disposed animations", disposedAnimations.size());
    }

    public void stopAllAnimations() {
        registered.forEach(ManagedAnimation::stop);
    }

    public Set<ManagedAnimation> animations() {
        return Collections.unmodifiableSet(registered);
    }
}