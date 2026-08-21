/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.basics.spriteanim.SpriteAnimContainer;
import de.amr.basics.spriteanim.SpriteAnimation;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

/**
 * Drives all sprite animations in the game. Provides a container where sprite animations are registered. Only active
 * animations stored in this container are animated.
 */
public class SpriteAnimationTimer {

    private static final int FPS = 60;

    private final Timeline animationTimer = new Timeline();

    public SpriteAnimationTimer() {
        animationTimer.setCycleCount(Animation.INDEFINITE);
    }

    public void attachAnimContainer(SpriteAnimContainer animContainer) {
        requireNonNull(animContainer);

        detachAnimationContainer();

        final var frame = new KeyFrame(Duration.seconds(1.0 / FPS), _ -> {
            for (SpriteAnimation animation : animContainer.activeAnimations()) {
                animation.tick();
            }
        });
        animationTimer.getKeyFrames().setAll(frame);
    }

    public void detachAnimationContainer() {
        animationTimer.stop();
        animationTimer.getKeyFrames().clear();
    }

    public void start() {
        animationTimer.playFromStart();
    }

    public void stop() {
        animationTimer.stop();
    }
}
