/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.Duration;

import static de.amr.pacmanfx.uilib.animation.AnimationSupport.pauseSec;

/**
 * Animation played when level is complete. Consists of the following steps:
 * <pre>
 *     Time (sec)         Action
 *     0.0                Start animation
 *     1.5                Hide ghosts
 *     2.0                Start n flashing cycles, each cycle takes 1/3 sec
 *     2.0 + n * 1/3 sec  Wait 1 sec
 * </pre>
 * After each flashing cycle, the flashing index is incremented. This is used by the Tengen play scene renderer to
 * draw a different map color for each flashing cycle (only for the non-ARCADE maps starting at level 28)
 */
public class LevelCompletedAnimation {

    public class FlashingState {
        private boolean flashing;
        private int flashingIndex;
        private final BooleanProperty highlighted = new SimpleBooleanProperty(false);

        private void setHighlighted(boolean value) {
            highlighted.set(value);
        }

        private void reset() {
            flashing = false;
            flashingIndex = 0;
            setHighlighted(false);
        }

        private void advanceFlashingIndex(int max) {
            if (flashingIndex + 1 < max) flashingIndex++;
        }

        public boolean isHighlighted() {
            return highlighted.get();
        }

        public boolean isFlashing() {
            return flashingAnimation != null && flashingAnimation.getStatus() == Animation.Status.RUNNING;
        }

        public int flashingIndex() {
            return flashingIndex;
        }
    }

    public static final int DEFAULT_SINGLE_FLASH_MILLIS = 333;
    public static final double GHOSTS_HIDING_DELAY = 1.5;

    private final FlashingState flashingState = new FlashingState();

    private final int singleFlashMillis;
    private final Runnable onFinished;

    private Animation animation;
    private Timeline flashingAnimation;

    public LevelCompletedAnimation(Runnable onFinished) {
        this(DEFAULT_SINGLE_FLASH_MILLIS, onFinished);
    }

    public LevelCompletedAnimation(int singleFlashMillis, Runnable onFinished) {
        this.singleFlashMillis = singleFlashMillis;
        this.onFinished = onFinished;
    }

    public FlashingState flashingState() {
        return flashingState;
    }

    public void play(GameLevel level) {
        if (animation == null) {
            createAnimation(level);
        }
        animation.playFromStart();
    }

    private Timeline createFlashing(int numFlashes) {
        var flashing = new Timeline(
            new KeyFrame(Duration.ZERO,                             _ -> flashingState.reset()),
            new KeyFrame(Duration.millis(singleFlashMillis * 0.25), _ -> flashingState.setHighlighted(true)),
            new KeyFrame(Duration.millis(singleFlashMillis * 0.75), _ -> flashingState.setHighlighted(false)),
            new KeyFrame(Duration.millis(singleFlashMillis),        _ -> flashingState.advanceFlashingIndex(numFlashes))
        );
        flashing.setCycleCount(numFlashes);
        return flashing;
    }

    private void createAnimation(GameLevel level) {
        final int numFlashes = level.numFlashes();
        flashingAnimation = createFlashing(numFlashes);
        final Animation hideGhostsAfterDelay = pauseSec(GHOSTS_HIDING_DELAY, () -> level.ghosts().forEach(Ghost::hide));
        animation = numFlashes > 0
            ? new SequentialTransition(hideGhostsAfterDelay, pauseSec(0.5), flashingAnimation, pauseSec(1))
            : new SequentialTransition(hideGhostsAfterDelay, pauseSec(1.5));
        if (onFinished != null) {
            animation.setOnFinished(_ -> onFinished.run());
        }
    }
}