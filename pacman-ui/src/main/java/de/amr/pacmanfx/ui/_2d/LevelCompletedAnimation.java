/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.RegisteredAnimation;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.Duration;

import static de.amr.pacmanfx.uilib.animation.AnimationSupport.pauseSec;
import static java.util.Objects.requireNonNull;

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
public class LevelCompletedAnimation extends RegisteredAnimation {

    public static final double GHOSTS_HIDING_DELAY = 1.5;

    private final GameLevel gameLevel;
    private final BooleanProperty highlighted = new SimpleBooleanProperty(false);
    private Timeline flashingAnimation;
    private int singleFlashMillis;
    private int flashingIndex;

    public LevelCompletedAnimation(AnimationRegistry animationRegistry, GameLevel gameLevel) {
        super(animationRegistry, "Level_Completed");
        this.gameLevel = requireNonNull(gameLevel);
        singleFlashMillis = 333;
    }

    @Override
    protected Animation createAnimationFX() {
        final int numFlashes = gameLevel.numFlashes();
        flashingAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, _ -> flashingIndex = 0),
            new KeyFrame(Duration.millis(singleFlashMillis * 0.25), _ -> highlighted.set(true)),
            new KeyFrame(Duration.millis(singleFlashMillis * 0.75), _ -> highlighted.set(false)),
            new KeyFrame(Duration.millis(singleFlashMillis), _ -> nextFlashingIndex(numFlashes))
        );
        flashingAnimation.setCycleCount(numFlashes);
        final Animation hideGhostsAfterDelay = pauseSec(GHOSTS_HIDING_DELAY, () -> gameLevel.ghosts().forEach(Ghost::hide));
        return numFlashes > 0
            ? new SequentialTransition(hideGhostsAfterDelay, pauseSec(0.5), flashingAnimation, pauseSec(1))
            : new SequentialTransition(hideGhostsAfterDelay, pauseSec(1.5));
    }

    private void nextFlashingIndex(int numFlashes) {
        if (flashingIndex + 1 < numFlashes) flashingIndex++;
    }

    public boolean isHighlighted() {
        return highlighted.get();
    }

    public boolean isFlashing() {
        return flashingAnimation != null && flashingAnimation.getStatus() == Animation.Status.RUNNING;
    }

    public void setSingleFlashMillis(int singleFlashMillis) {
        this.singleFlashMillis = singleFlashMillis;
        invalidate();
    }

    public int flashingIndex() {
        return flashingIndex;
    }
}