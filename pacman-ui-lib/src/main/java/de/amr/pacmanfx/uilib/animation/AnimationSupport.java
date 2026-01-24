/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.Transition;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

public interface AnimationSupport {
    static Transition doNow(Runnable action) {
        requireNonNull(action);
        var transition = new Transition() {
            @Override
            protected void interpolate(double t) {}
        };
        transition.setOnFinished(e -> action.run());
        return transition;
    }

    /**
     * Pauses for the given number of seconds.
     *
     * @param seconds number of seconds
     * @return pause transition
     */
    static PauseTransition pauseSec(double seconds) {
        return new PauseTransition(Duration.seconds(seconds));
    }

    /**
     * Prepends a pause of the given duration (in seconds) before the given action is executed. Note that you have to call
     * {@link Animation#play()} to execute the action!
     * <p>
     * NOTE: Do NOT start an animation in the action!
     *
     * @param seconds number of seconds to wait before the action is executed
     * @param action       code to run
     * @return pause transition
     */
    static PauseTransition pauseSecThen(double seconds, Runnable action) {
        requireNonNull(action);
        var pause = new PauseTransition(Duration.seconds(seconds));
        pause.setOnFinished(e -> action.run());
        return pause;
    }
}
