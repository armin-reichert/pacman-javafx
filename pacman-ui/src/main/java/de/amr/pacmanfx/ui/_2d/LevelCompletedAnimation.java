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
import javafx.util.Duration;

import java.util.Optional;

import static de.amr.pacmanfx.uilib.animation.AnimationSupport.doNow;
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

    public interface FlashingState {
        boolean isHighlighted();
        boolean isFlashing();
        int flashingIndex();
    }

    public static final int DEFAULT_SINGLE_FLASH_MILLIS = 333;
    public static final double GHOSTS_HIDING_DELAY = 1.5;

    private static class FlashingAnimation implements FlashingState {

        private final Timeline timeline;
        private boolean highlighted;
        private int cycle;

        private FlashingAnimation(int numFlashes, long singleFlashMillis) {
            timeline = new Timeline(
                new KeyFrame(Duration.ZERO,                             _ -> reset()),
                new KeyFrame(Duration.millis(singleFlashMillis * 0.25), _ -> setHighlighted(true)),
                new KeyFrame(Duration.millis(singleFlashMillis * 0.75), _ -> setHighlighted(false)),
                new KeyFrame(Duration.millis(singleFlashMillis),        _ -> enterNextCycle(numFlashes))
            );
            timeline.setCycleCount(numFlashes);
        }

        private void reset() {
            cycle = 0;
            setHighlighted(false);
        }

        private void setHighlighted(boolean value) {
            highlighted = value;
        }

        private void enterNextCycle(int max) {
            if (cycle + 1 < max) cycle++;
        }

        @Override
        public boolean isHighlighted() {
            return highlighted;
        }

        @Override
        public boolean isFlashing() {
            return timeline.getStatus() == Animation.Status.RUNNING;
        }

        @Override
        public int flashingIndex() {
            return cycle;
        }
    }

    private final int singleFlashMillis;
    private final Runnable onFinished;

    private FlashingAnimation flashingAnimation;
    private Animation animation;

    public LevelCompletedAnimation(Runnable onFinished) {
        this(DEFAULT_SINGLE_FLASH_MILLIS, onFinished);
    }

    public LevelCompletedAnimation(int singleFlashMillis, Runnable onFinished) {
        this.singleFlashMillis = singleFlashMillis;
        this.onFinished = onFinished;
    }

    public Optional<FlashingState> flashingState() {
        return Optional.ofNullable(flashingAnimation);
    }

    public void play(GameLevel level) {
        if (animation == null) {
            createAnimation(level);
        }
        animation.playFromStart();
    }

    private void createAnimation(GameLevel level) {
        if (level.numFlashes() > 0) {
            flashingAnimation = new FlashingAnimation(level.numFlashes(), singleFlashMillis);
            animation = new SequentialTransition(
                doNow(() -> level.ghosts().forEach(Ghost::hide)),
                pauseSec(0.5),
                flashingAnimation.timeline,
                pauseSec(1));
        }
        else {
            animation = new SequentialTransition(
                doNow(() -> level.ghosts().forEach(Ghost::hide)),
                pauseSec(1.5));
        }
        animation.setDelay(Duration.seconds(GHOSTS_HIDING_DELAY));
        if (onFinished != null) {
            animation.setOnFinished(_ -> onFinished.run());
        }
    }
}