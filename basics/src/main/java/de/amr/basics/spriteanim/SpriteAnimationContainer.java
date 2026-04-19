/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.spriteanim;

import org.tinylog.Logger;

import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class SpriteAnimationContainer {

    private static class LazyThreadSafeSingletonHolder {
        static final SpriteAnimationContainer SINGLETON = new SpriteAnimationContainer();
    }

    public static SpriteAnimationContainer instance() {
        return LazyThreadSafeSingletonHolder.SINGLETON;
    }

    private final Set<SpriteAnimation> active = new HashSet<>();
    private final Set<SpriteAnimation> pendingAdd = new HashSet<>();
    private final Set<SpriteAnimation> pendingRemove = new HashSet<>();
    private boolean clearRequest;

    private SpriteAnimationContainer() {}

    public void update(long now) {

        if (clearRequest) {
            active.clear();
            pendingAdd.clear();
            pendingRemove.clear();
            clearRequest = false;
            if (Logger.isTraceEnabled()) {
                Logger.trace("Sprite animation cache cleared");
            }
        }

        // Apply pending removals
        if (!pendingRemove.isEmpty()) {
            active.removeAll(pendingRemove);
            pendingRemove.clear();
            if (Logger.isTraceEnabled()) {
                Logger.trace("Sprite animation unregistered (cache size={})", active.size());
            }
        }

        // Apply pending additions
        if (!pendingAdd.isEmpty()) {
            active.addAll(pendingAdd);
            pendingAdd.clear();
            if (Logger.isTraceEnabled()) {
                Logger.trace("Sprite animations registered (cache size={})", active.size());
            }
        }

        // Now safe to iterate
        for (SpriteAnimation animation : active) {
            animation.update(now);
        }
    }

    public void register(SpriteAnimation animation) {
        requireNonNull(animation);
        pendingAdd.add(animation);
    }

    public void unregister(SpriteAnimation animation) {
        requireNonNull(animation);
        pendingRemove.add(animation);
    }

    public void clear() {
        clearRequest = true;
    }
}
