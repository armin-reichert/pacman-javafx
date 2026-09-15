/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.level.GameLevel;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;

import java.util.Optional;

import static de.amr.basics.util.Ufx.pauseSec;
import static de.amr.basics.util.Ufx.pauseSecThen;

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

    /** Default duration of a single flashing cycle in milliseconds (~1/3 sec). */
    public static final int DEFAULT_SINGLE_FLASH_MILLIS = 333;

    private final int singleFlashMillis;

    private final Runnable onFinished;

    private FlashingAnimation flashingAnimation;

    private Animation animationSequence;

    public LevelCompletedAnimation(Runnable onFinished) {
        this(DEFAULT_SINGLE_FLASH_MILLIS, onFinished);
    }

    public LevelCompletedAnimation(int singleFlashMillis, Runnable onFinished) {
        this.singleFlashMillis = singleFlashMillis;
        this.onFinished = onFinished;
    }

    /**
     * Returns the current flashing state if flashing is active for this level.
     *
     * @return optional flashing state (empty if the level has no flashing)
     */
    public Optional<FlashingState> optFlashingState() {
        if (flashingAnimation == null) {
            return Optional.empty();
        }
        return Optional.of(flashingAnimation.flashingState());
    }

    /** Starts (or restarts) the level-complete animation. */
    public void play(GameLevel level, int numFlashes) {
        createAnimation(level, numFlashes);
        animationSequence.playFromStart();
    }

    private void createAnimation(GameLevel level, int numFlashes) {
        final Animation hideGhostsAnimation = pauseSecThen(1.5,
            () ->level.entities().ghosts().forEach(GameEntity::hide)
        );

        if (numFlashes != 0) {
            flashingAnimation = new FlashingAnimation(numFlashes, singleFlashMillis);
            animationSequence = new SequentialTransition(
                hideGhostsAnimation,
                pauseSec(0.5),
                flashingAnimation.animation(),
                pauseSec(1)
            );
        }
        else {
            animationSequence = new SequentialTransition(hideGhostsAnimation, pauseSec(1.5));
        }

        if (onFinished != null) {
            animationSequence.setOnFinished(_ -> onFinished.run());
        }
    }
}