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

    private final SpriteAnimationContainer animationContainer;
    private final Animation timer;

    public SpriteAnimationManager(int fps) {
        animationContainer = new SpriteAnimationContainer();
        timer = new Timeline(new KeyFrame(Duration.seconds(1.0 / fps), _ -> {
            for (SpriteAnimation animation : animationContainer.activeAnimations()) {
                animation.tick();
            }
        }));
        timer.setCycleCount(Animation.INDEFINITE);
    }

    public SpriteAnimationContainer animationContainer() {
        return animationContainer;
    }

    public void startAnimationTimer() {
        timer.playFromStart();
    }

    public void stopAnimationTimer() {
        timer.stop();
    }
}
