/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.spriteanim;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * Drives all sprite animations in the game. Provides a container where sprite animations are registered. Only active
 * animations stored in this container are animated.
 */
public class SpriteAnimationSystem {

    private final Timeline timeline;

    private final SpriteAnimationPool pool;

    public SpriteAnimationSystem(int fps) {
        pool = new SpriteAnimationPool();

        //TODO Use AnimationTimer class instead?
        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        final var frame = new KeyFrame(Duration.seconds(1.0 / fps), _ -> {
            for (SpriteAnimation animation : pool.activeAnimations()) {
                animation.tick(this);
            }
        });
        timeline.getKeyFrames().setAll(frame);
    }

    public void start() {
        timeline.playFromStart();
    }

    public void stop() {
        timeline.stop();
    }

    public SpriteAnimationPool pool() {
        return pool;
    }
}
