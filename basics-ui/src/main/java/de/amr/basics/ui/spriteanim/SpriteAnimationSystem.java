/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.spriteanim;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

/**
 * Drives all sprite animations in the game. Provides a container where sprite animations are registered. Only active
 * animations stored in this container are animated.
 */
public class SpriteAnimationSystem {

    private final Timeline timeline;

    private final SpriteAnimationContainer animationContainer;

    public SpriteAnimationSystem(int fps) {
        animationContainer = new SpriteAnimationContainer();
        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        final var frame = new KeyFrame(Duration.seconds(1.0 / fps), _ -> {
            for (SpriteAnimation animation : animationContainer.activeAnimations()) {
                animation.tick();
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

    public SpriteAnimationContainer animationContainer() {
        return animationContainer;
    }
}
