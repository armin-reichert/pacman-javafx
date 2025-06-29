/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
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

import static de.amr.pacmanfx.uilib.Ufx.doAfterSec;
import static de.amr.pacmanfx.uilib.Ufx.pauseSec;

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
public class LevelFlashingAnimation {

    private final SequentialTransition animation;
    private int flashingIndex;
    private boolean highlighted;

    public LevelFlashingAnimation(GameLevel level, int singleFlashMillis) {
        int numFlashes = level.data().numFlashes();
        animation = new SequentialTransition(
            doAfterSec(1.5, () -> level.ghosts().forEach(Ghost::hide)),
            doAfterSec(0.5, numFlashes > 0 ? flashingAnimation(numFlashes, singleFlashMillis) : pauseSec(0)),
            pauseSec(1)
        );
    }

    private Animation flashingAnimation(int numFlashes, int singleFlashMillis) {
        var flashingAnimation = new Timeline(
            new KeyFrame(Duration.millis(singleFlashMillis * 0.25), e -> highlighted = true),
            new KeyFrame(Duration.millis(singleFlashMillis * 0.75), e -> highlighted = false),
            new KeyFrame(Duration.millis(singleFlashMillis), e -> {
                if (flashingIndex + 1 < numFlashes) flashingIndex++;
            })
        );
        flashingAnimation.setCycleCount(numFlashes);
        return flashingAnimation;
    }

    public void play() {
        flashingIndex = 0;
        highlighted = false;
        animation.play();
    }

    public boolean isRunning() {
        return animation.getStatus() == Animation.Status.RUNNING;
    }

    public void setOnFinished(Runnable action) {
        animation.setOnFinished(e -> action.run());
    }

    public int flashingIndex() {
        return flashingIndex;
    }

    public boolean isHighlighted() {
        return highlighted;
    }
}