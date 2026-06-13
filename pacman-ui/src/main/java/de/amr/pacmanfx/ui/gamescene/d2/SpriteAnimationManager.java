/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import javafx.animation.AnimationTimer;

public class SpriteAnimationManager {

    private final SpriteAnimationContainer context;
    private final AnimationTimer timer;

    public SpriteAnimationManager() {
        context = new SpriteAnimationContainer();
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                for (SpriteAnimation sa : context.activeAnimations()) {
                    sa.update(now);
                }
            }
        };
    }

    public SpriteAnimationContainer animations() {
        return context;
    }

    public void startAnimationTimer() {
        timer.start();
    }

    public void stopAnimationTimer() {
        timer.stop();
    }
}
