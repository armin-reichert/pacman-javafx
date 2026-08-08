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

        final ManagedAnimation ma = animationMap.get(key);
        if (ma != null) {
            Logger.warn("Animation '{}' was already registered, old one gets disposed and overwritten", animation.name());
            ma.dispose();
        }
        animationMap.put(key, animation);
        Logger.info("Animation '{}' registered, key='{}'", animation.name(), key);
    }

    public <T extends ManagedAnimation> T requireAnimation(Object key, Class<T> expectedClass) {
        requireNonNull(key);
        requireNonNull(expectedClass);

        final ManagedAnimation ma = animationMap.get(key);
        if (ma == null) {
            throw new IllegalArgumentException("No animation with key='%s' exists".formatted(key));
        }
        if (expectedClass.isInstance(ma)) {
            return expectedClass.cast(ma);
        }
        throw new IllegalArgumentException("Animation with key='%s' has wrong type: %s (expected: %s)".formatted(
            key, ma.getClass().getSimpleName(), expectedClass.getSimpleName()));
    }

    public ManagedAnimation requireAnimation(Object key) {
        final ManagedAnimation ma = animationMap.get(key);
        if (ma == null) {
            throw new IllegalArgumentException("No animation with key='%s' exists".formatted(key));
        }
        return ma;
    }

    public Optional<ManagedAnimation> optAnimation(Object key) {
        return Optional.ofNullable(animationMap.get(key));
    }

    public <T extends ManagedAnimation> Optional<T> optAnimation(Object key, Class<T> expectedClass) {
        requireNonNull(key);
        requireNonNull(expectedClass);

        final ManagedAnimation ma = animationMap.get(key);
        if (ma == null) {
            return Optional.empty();
        }
        if (expectedClass.isInstance(ma)) {
            return Optional.of(expectedClass.cast(ma));
        }
        throw new IllegalArgumentException("Animation with key='%s' has wrong type: %s (expected: %s)".formatted(
            key, ma.getClass().getSimpleName(), expectedClass.getSimpleName()));
    }

    public void dispose() {
        stopAllAnimations();
        final int count = animationMap.size();
        animationMap.values().forEach(ManagedAnimation::dispose);
        animationMap.clear();
        Logger.info("Disposed {} managed animations", count);
    }

    public void stopAllAnimations() {
        animationMap.values().forEach(ManagedAnimation::stop);
    }

    public Collection<ManagedAnimation> animations() {
        return Collections.unmodifiableCollection(animationMap.values());
    }
}