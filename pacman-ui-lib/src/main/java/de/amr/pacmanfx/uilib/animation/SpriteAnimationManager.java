/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.animation;

import javafx.animation.AnimationTimer;

import java.util.HashSet;
import java.util.Set;

public class SpriteAnimationManager extends AnimationTimer {

    private final Set<SpriteAnimation> animations = new HashSet<>();

    public void registerAnimation(SpriteAnimation animation) {
        animations.add(animation);
    }

    public void unregisterAnimation(SpriteAnimation animation) {
        animations.remove(animation);
    }

    public void clear() {
        animations.clear();
    }

    @Override
    public void handle(long now) {
        for (SpriteAnimation animation : animations) {
            animation.update(now);
        }
    }
}
