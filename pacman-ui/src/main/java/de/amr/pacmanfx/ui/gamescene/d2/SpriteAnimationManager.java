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
    private Animation timer;

    public SpriteAnimationManager() {
        context = new SpriteAnimationContainer();
        createAnimationTimer();
    }

    public SpriteAnimationContainer animations() {
        return context;
    }

    public void createAnimationTimer() {
        timer = new Timeline(new KeyFrame(Duration.seconds(1.0 / 60), e -> {
            for (SpriteAnimation animation : context.activeAnimations()) {
                animation.tick();
            }
        }));
        timer.setCycleCount(Animation.INDEFINITE);
    }

    public void startAnimationTimer() {
        timer.playFromStart();
    }

    public void stopAnimationTimer() {
        timer.stop();
    }
}
