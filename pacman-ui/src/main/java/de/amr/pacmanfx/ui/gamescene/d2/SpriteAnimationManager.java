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

    private final SpriteAnimationContainer context;
    private final Animation timer;

    public SpriteAnimationManager(int fps) {
        context = new SpriteAnimationContainer();
        timer = new Timeline(new KeyFrame(Duration.seconds(1.0 / fps), _ -> {
            for (SpriteAnimation animation : context.activeAnimations()) {
                animation.tick();
            }
        }));
        timer.setCycleCount(Animation.INDEFINITE);
    }

    public SpriteAnimationContainer animations() {
        return context;
    }

    public void startAnimationTimer() {
        timer.playFromStart();
    }

    public void stopAnimationTimer() {
        timer.stop();
    }
}
