/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.animation;

import javafx.animation.AnimationTimer;

import java.util.ArrayList;
import java.util.List;

public class SpriteAnimationTimer extends AnimationTimer {

    private final List<SpriteAnimation> animations = new ArrayList<>();

    public void registerAnimation(SpriteAnimation animation) {
        animations.add(animation);
    }

    public void unregisterAnimation(SpriteAnimation animation) {
        animations.remove(animation);
    }

    @Override
    public void handle(long now) {
        for (SpriteAnimation animation : animations) {
            animation.update(now);
        }
    }
}
