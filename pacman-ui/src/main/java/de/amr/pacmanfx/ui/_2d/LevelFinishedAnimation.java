/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.animation.*;
import javafx.util.Duration;
import org.tinylog.Logger;

/**
 * Animation played when level is complete. Consists of the following steps:
 * <pre>
 *     Time (sec)         Action
 *     0.0                Start animation
 *     1.5                Hide ghosts
 *     2.0                Start n flashing cycles, each cycle takes 1/3 sec
 *     2.0 + n * 1/3 sec  Wait for 1 sec, then run the action specified for finishing the animation
 * </pre>
 * After each flashing cycle, the flashing index is incremented. This is used by the Tengen play scene renderer to
 * draw a different map color for each flashing cycle (only for the non-ARCADE maps starting at level 28)
 */
public class LevelFinishedAnimation {

    private final SequentialTransition animation;
    private int flashingIndex;
    private boolean highlighted;

    public LevelFinishedAnimation(GameLevel level) {
        animation = new SequentialTransition(
            Ufx.doAfterSec(1.5, () -> level.ghosts().forEach(Actor::hide)),
            new PauseTransition(Duration.seconds(0.5)),
            createMazeFlashingAnimation(level.data().numFlashes()),
            new PauseTransition(Duration.seconds(1))
        );
        animation.setOnFinished(e -> {
            highlighted = false;
            logState("end of animation");
        });
    }

    private Animation createMazeFlashingAnimation(int numFlashes) {
        return new Transition() {
            {
                setCycleDuration(Duration.millis(333));
                setCycleCount(numFlashes);
                setInterpolator(Interpolator.LINEAR);
            }

            @Override
            protected void interpolate(double t) {
                //TODO WTF
                if (Math.abs(t - 0.5) < 0.1 && !highlighted) {
                    highlighted = true;
                    logState("half time of flash cycle");
                }
                else if (t == 1) {
                    highlighted = false;
                    flashingIndex++;
                    logState("end of flash cycle");
                }
            }
        };
    }

    public SequentialTransition getAnimation() {
        return animation;
    }

    public int getFlashingIndex() {
        return flashingIndex;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    private void logState(String where) {
        Logger.info("At {}: highlighted={} flashingIndex={}", where, highlighted, flashingIndex);
    }
}
