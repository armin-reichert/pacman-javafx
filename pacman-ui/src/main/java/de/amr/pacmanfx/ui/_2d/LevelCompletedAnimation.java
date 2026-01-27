/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
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

import static de.amr.pacmanfx.uilib.animation.AnimationSupport.pauseSec;
import static de.amr.pacmanfx.uilib.animation.AnimationSupport.pauseSecThen;
import static java.util.Objects.requireNonNull;

/**
 * Animation played when a level is complete.
 * <p>
 * Consists of the following steps:
 * <pre>
 *     Time (sec)         Action
 *     0.0                Start animation
 *     1.5                Hide ghosts
 *     2.0                Start n flashing cycles, each cycle takes ~1/3 sec
 *     2.0 + n * 1/3 sec  Wait 1 sec
 * </pre>
 * </p>
 * <p>
 * After each flashing cycle, the flashing index is incremented. This is used by the Tengen play scene renderer to
 * draw a different map color for each flashing cycle (only for the non-ARCADE maps starting at level 28).
 * </p>
 */
public class LevelCompletedAnimation {

    /**
     * State of the flashing part of the level-complete animation. Used by renderers to query the current flashing status.
     */
    public interface FlashingState {

        /** @return true if the map should be drawn highlighted (bright) in the current frame */
        boolean isHighlighted();

        /** @return true if the flashing animation is currently running */
        boolean isFlashing();

        /** @return the current flashing cycle index (0-based) */
        int flashingIndex();
    }

    /** Default duration of a single flashing cycle in milliseconds (~1/3 sec). */
    public static final int DEFAULT_SINGLE_FLASH_MILLIS = 333;

    private static class FlashingAnimation implements FlashingState {

        private final Timeline timeline;
        private boolean highlighted;
        private int index;

        private FlashingAnimation(int numFlashes, long singleFlashMillis) {
            timeline = new Timeline(
                new KeyFrame(Duration.ZERO,                             _ -> highlighted = false),
                new KeyFrame(Duration.millis(singleFlashMillis * 0.25), _ -> highlighted = true),
                new KeyFrame(Duration.millis(singleFlashMillis * 0.75), _ -> highlighted = false),
                new KeyFrame(Duration.millis(singleFlashMillis * 1.00), _ -> ++index)
            );
            timeline.setCycleCount(numFlashes);
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
            return index;
        }
    }

    private final GameLevel level;
    private final int singleFlashMillis;
    private final Runnable onFinished;

    private FlashingAnimation flashingAnimation;
    private Animation animation;

    /**
     * Creates a level-complete animation for the given level using the default flashing duration.
     *
     * @param level      the game level
     * @param onFinished callback executed when the animation finishes
     */
    public LevelCompletedAnimation(GameLevel level, Runnable onFinished) {
        this(level, DEFAULT_SINGLE_FLASH_MILLIS, onFinished);
    }

    /**
     * Creates a level-complete animation for the given level.
     *
     * @param level             the game level
     * @param singleFlashMillis duration of a single flashing cycle in milliseconds
     * @param onFinished        callback executed when the animation finishes
     */
    public LevelCompletedAnimation(GameLevel level, int singleFlashMillis, Runnable onFinished) {
        this.level = requireNonNull(level);
        this.singleFlashMillis = singleFlashMillis;
        this.onFinished = onFinished;
    }

    /**
     * Returns the current flashing state if flashing is active for this level.
     *
     * @return optional flashing state (empty if the level has no flashing)
     */
    public Optional<FlashingState> flashingState() {
        return Optional.ofNullable(flashingAnimation);
    }

    /** Starts (or restarts) the level-complete animation. */
    public void play() {
        if (animation == null) {
            createAnimation();
        }
        animation.playFromStart();
    }

    private void createAnimation() {
        final Animation hideGhosts = pauseSecThen(1.5, this::hideGhosts);
        if (level.numFlashes() > 0) {
            flashingAnimation = new FlashingAnimation(level.numFlashes(), singleFlashMillis);
            animation = new SequentialTransition(
                    hideGhosts,
                    pauseSec(0.5),
                    flashingAnimation.timeline,
                    pauseSec(1)
            );
        } else {
            animation = new SequentialTransition(hideGhosts, pauseSec(1.5));
        }
        if (onFinished != null) {
            animation.setOnFinished(_ -> onFinished.run());
        }
    }

    private void hideGhosts() {
        level.ghosts().forEach(Ghost::hide);
    }
}