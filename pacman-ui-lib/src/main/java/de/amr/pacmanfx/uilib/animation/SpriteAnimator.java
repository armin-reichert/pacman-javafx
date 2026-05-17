package de.amr.pacmanfx.uilib.animation;

import de.amr.basics.spriteanim.SpriteAnimationContainer;
import javafx.animation.AnimationTimer;

public class SpriteAnimator extends SpriteAnimationContainer {

    private final AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            updateAnimations(now);
        }
    };

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }
}
