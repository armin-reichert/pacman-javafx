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
    public <T extends ManagedAnimation> T animation(Object key, Class<T> type) {
        requireNonNull(key);
        requireNonNull(type);
        final ManagedAnimation ma = animationMap.get(key);
        if (ma == null) {
            Logger.error("No animation with key='{}' exists", key);
            return null;
        }
        if (type.isInstance(ma)) {
            return (T) ma;
        }
        Logger.error("Animation key='{}' has wrong type: {}, expected: {}", key, ma.getClass().getSimpleName(), type.getSimpleName());
        return null;
    }

    public ManagedAnimation animation(Object key) {
        return animationMap.get(key);
    }

    public Optional<ManagedAnimation> optAnimation(Object key) {
        return Optional.ofNullable(animation(key));
    }

    public <T extends ManagedAnimation> Optional<T> optAnimation(Object key, Class<T> type) {
        return Optional.ofNullable(animation(key, type));
    }

    public void dispose() {
        final int count = animationMap.size();
        stopAllAnimations();
        animationMap.values().forEach(ManagedAnimation::dispose);
        animationMap.clear();
        Logger.info("Disposed {} animations", count);
    }

    public void stopAllAnimations() {
        animationMap.values().forEach(ManagedAnimation::stop);
    }

    public Collection<ManagedAnimation> animations() {
        return Collections.unmodifiableCollection(animationMap.values());
    }
}