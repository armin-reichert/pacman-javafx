/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.animation;

import javafx.animation.AnimationTimer;
import org.tinylog.Logger;

import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class SpriteAnimationDriver {

    private final AnimationTimer timer;
    private final Set<SpriteAnimation> animations = new HashSet<>();

    public SpriteAnimationDriver() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                for (SpriteAnimation animation : animations) {
                    animation.update(now);
                }
            }
        };
    }

    public void startAnimation() {
        timer.start();
    }

    public void stopAnimation() {
        timer.stop();
    }

    public void register(SpriteAnimation animation) {
        requireNonNull(animation);
        animations.add(animation);
        Logger.debug("Sprite animation registered (cache size={})", animations.size());
    }

    public void clear() {
        animations.clear();
        Logger.info("Sprite animation cache cleared");
    }
}
