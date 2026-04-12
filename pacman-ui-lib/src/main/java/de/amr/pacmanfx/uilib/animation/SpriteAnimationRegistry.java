/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.animation;

import javafx.animation.AnimationTimer;
import org.tinylog.Logger;

import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class SpriteAnimationRegistry {

    private final AnimationTimer animationTimer;
    private final Set<SpriteAnimation> spriteAnimations = new HashSet<>();

    public SpriteAnimationRegistry() {
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                for (SpriteAnimation animation : spriteAnimations) {
                    animation.update(now);
                }
            }
        };
    }

    public void startAnimationTimer() {
        animationTimer.start();
    }

    public void stopAnimationTimer() {
        animationTimer.stop();
    }

    public void registerAnimation(SpriteAnimation animation) {
        requireNonNull(animation);
        spriteAnimations.add(animation);
        Logger.info("Sprite animation registered (cache size={})", spriteAnimations.size());
    }

    public void clearAnimations() {
        spriteAnimations.clear();
        Logger.info("Sprite animation cache cleared");
    }
}
