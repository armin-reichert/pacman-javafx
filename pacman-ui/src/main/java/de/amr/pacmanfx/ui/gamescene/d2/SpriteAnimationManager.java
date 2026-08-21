/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimContainer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * Drives all sprite animations in the game. Provides a container where sprite animations are registered. Only active
 * animations stored in this container are animated.
 */
public class SpriteAnimationManager {

    private final SpriteAnimContainer animations;
    private final Animation animationTimer;

    public SpriteAnimationManager() {
        this(60);
    }

    public SpriteAnimationManager(int fps) {
        animations = new SpriteAnimContainer();
        final Duration frameDuration = Duration.seconds(1.0 / fps);
        final var timerTick = new KeyFrame(frameDuration, _ -> {
            for (SpriteAnimation animation : animations.activeAnimations()) {
                animation.tick();
            }
        });
        animationTimer = new Timeline(timerTick);
        animationTimer.setCycleCount(Animation.INDEFINITE);
    }

    public SpriteAnimContainer animContainer() {
        return animations;
    }

    public void startAnimationTimer() {
        animationTimer.playFromStart();
    }

    public void stopAnimationTimer() {
        animationTimer.stop();
    }
}
