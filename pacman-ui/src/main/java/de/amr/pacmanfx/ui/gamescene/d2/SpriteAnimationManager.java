/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class SpriteAnimationManager {

    private final SpriteAnimationContainer animations;
    private final Animation timer;

    public SpriteAnimationManager() {
        this(60);
    }

    public SpriteAnimationManager(int fps) {
        animations = new SpriteAnimationContainer();
        timer = new Timeline(new KeyFrame(Duration.seconds(1.0 / fps), _ -> {
            for (SpriteAnimation animation : animations.activeAnimations()) {
                animation.tick();
            }
        }));
        timer.setCycleCount(Animation.INDEFINITE);
    }

    public SpriteAnimationContainer animations() {
        return animations;
    }

    public void startAnimationTimer() {
        timer.playFromStart();
    }

    public void stopAnimationTimer() {
        timer.stop();
    }
}
