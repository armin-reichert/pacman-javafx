package de.amr.pacmanfx.ui.gamescene.d2;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class FlashingAnimation {

    private final Timeline animation;
    private boolean highlighted;
    private int index;

    public FlashingAnimation(int numFlashes, long singleFlashMillis) {
        animation = new Timeline(
            new KeyFrame(Duration.ZERO, _ -> highlighted = false),
            new KeyFrame(Duration.millis(singleFlashMillis * 0.25), _ -> highlighted = true),
            new KeyFrame(Duration.millis(singleFlashMillis * 0.75), _ -> highlighted = false),
            new KeyFrame(Duration.millis(singleFlashMillis * 1.00), _ -> ++index)
        );
        animation.setCycleCount(numFlashes);
    }

    public Timeline animation() {
        return animation;
    }

    public FlashingState flashingState() {
        return new FlashingState(highlighted, animation.getStatus() == Animation.Status.RUNNING, index);
    }
}
