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

    @SuppressWarnings("unchecked")
    public <T extends ManagedAnimation> T animation(String key, Class<T> type) {
        requireNonNull(key);
        requireNonNull(type);
        final ManagedAnimation managedAnimation = animationMap.get(key);
        if (managedAnimation == null) {
            Logger.error("No animation with key '{}' exists");
            throw new NoSuchElementException();
        }
        if (type.isInstance(managedAnimation)) {
            return (T) managedAnimation;
        }
        Logger.error("Animation with key '{}' has wrong type: {}, expected: {}",
            key, managedAnimation.getClass().getSimpleName(), type.getSimpleName());
        throw new NoSuchElementException();
    }

    public ManagedAnimation animation(String key) {
        return animationMap.get(key);
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