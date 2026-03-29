/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.animation;

import org.tinylog.Logger;

import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * A registry for managed animations.
 */
public class AnimationRegistry {

    private final Map<Object, ManagedAnimation> animationMap = new HashMap<>();

    public void register(Object key, ManagedAnimation animation) {
        requireNonNull(key);
        requireNonNull(animation);
        final ManagedAnimation managedAnimation = animationMap.get(key);
        if (managedAnimation != null) {
            Logger.warn("Animation '{}' is already registered, will be disposed and overwritten", animation.label());
            managedAnimation.dispose();
        }
        animationMap.put(key, animation);
        Logger.info("Animation '{}' registered, key='{}'", animation.label(), key);
    }

    public void clear() {
        stopAllAnimations();
        animationMap.values().forEach(ManagedAnimation::dispose);
        garbageCollect();
    }

    public void garbageCollect() {
        var disposedAnimations = animationMap.values().stream().filter(ManagedAnimation::disposed).toList();
        disposedAnimations.forEach(animationMap::remove);
        Logger.info("Removed {} disposed animations", disposedAnimations.size());
    }

    public void stopAllAnimations() {
        animationMap.values().forEach(ManagedAnimation::stop);
    }

    public Collection<ManagedAnimation> animations() {
        return Collections.unmodifiableCollection(animationMap.values());
    }
}