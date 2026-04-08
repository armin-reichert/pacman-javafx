/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.animation;

import javafx.animation.AnimationTimer;
import org.tinylog.Logger;

import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class SpriteAnimationManager extends AnimationTimer {

    private final Set<SpriteAnimation> cache = new HashSet<>();

    public void registerAnimation(SpriteAnimation animation) {
        requireNonNull(animation);
        cache.add(animation);
        Logger.info("Sprite animation registered (cache size={})", cache.size());
    }

    public void clearCache() {
        cache.clear();
        Logger.info("Sprite animation cache cleared");
    }

    @Override
    public void handle(long now) {
        for (SpriteAnimation animation : cache) {
            animation.update(now);
        }
    }
}
