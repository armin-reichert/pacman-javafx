/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;

import java.util.function.Consumer;

/**
 * A configurable clock that drives the simulation and UI updates of the game.
 * <p>
 * A {@code GameClock} produces periodic "ticks" at a target frame rate. Each tick may execute:
 * <ul>
 *   <li>a <em>permanent action</em>, which always runs, regardless of pause state</li>
 *   <li>a <em>pausable action</em>, which runs only when the clock is not paused</li>
 * </ul>
 * Implementations may use different timing backends (e.g., Timeline in JavaFX),
 * but all expose the same control surface for starting, stopping, pausing, and stepping
 * the simulation.
 * <p>
 * The clock also provides optional time‑measurement logging and basic statistics such as
 * ticks per second and total tick counts.
 */
public interface GameClock {

    /**
     * Default target frame rate in frames per second.
     */
    int DEFAULT_TARGET_FRAME_RATE = 60;

    /**
     * Installs an error handler that is invoked when either the permanent or
     * pausable action throws an exception during a tick.
     * <p>
     * If no handler is set, implementations may choose to log the exception or
     * stop the clock.
     *
     * @param errorHandler the handler to invoke on tick errors
     */
    void setErrorHandler(Consumer<Throwable> errorHandler);

    /**
     * Sets the action that is executed only when the clock is not paused.
     * <p>
     * This action typically contains the simulation update logic.
     *
     * @param action the pausable action to run on each tick
     */
    void setPausableAction(Runnable action);

    /**
     * Sets the action that is executed on every tick, regardless of pause state.
     * <p>
     * This action is typically used for tasks such as UI updates or time measurement.
     *
     * @param action the permanent action to run on each tick
     */
    void setPermanentAction(Runnable action);

    /**
     * Returns the property representing the target frame rate.
     * Changing this property rebuilds the underlying timing mechanism.
     *
     * @return the target frame rate property
     */
    DoubleProperty targetFrameRateProperty();

    /**
     * Returns the current target frame rate in frames per second.
     *
     * @return the target FPS
     */
    double targetFrameRate();

    /**
     * Sets the desired target frame rate. The clock is rebuilt automatically
     * to match the new rate.
     *
     * @param fps the new target frames per second
     */
    void setTargetFrameRate(double fps);

    /**
     * Returns the paused property. When {@code true}, the pausable action
     * is skipped on each tick.
     *
     * @return the paused property
     */
    BooleanProperty pausedProperty();

    /**
     * Sets whether the clock is paused.
     *
     * @param b {@code true} to pause the clock, {@code false} to unpause
     */
    void setPaused(boolean b);

    /**
     * Returns whether the clock is currently paused.
     *
     * @return {@code true} if paused
     */
    boolean isPaused();

    /**
     * Returns the property controlling whether time‑measurement logging is enabled.
     * Implementations may use this flag to print timing diagnostics.
     *
     * @return the time‑measurement property
     */
    BooleanProperty timeMeasuredProperty();

    /**
     * Starts the clock. If the clock is paused, it is automatically unpaused.
     * If the clock is already running, this method has no effect.
     */
    void start();

    /**
     * Stops the clock. No further ticks occur until {@link #start()} is called.
     * Stopping the clock does not reset tick counters.
     */
    void stop();

    /**
     * Returns whether the clock is currently running.
     *
     * @return {@code true} if the clock is active
     */
    boolean isRunning();

    /**
     * Returns the number of ticks executed during the last measured second.
     * This value is updated once per second when time measurement is enabled.
     *
     * @return ticks per second during the last measurement interval
     */
    double fps();

    /**
     * Returns the total number of ticks executed since the clock was created.
     *
     * @return the total tick count
     */
    long tickCount();

    /**
     * Returns the number of pausable updates executed. This count increases
     * only when the clock is not paused.
     *
     * @return the number of pausable updates
     */
    long pausableUpdatesCount();

    /**
     * Executes a fixed number of simulation steps synchronously.
     * <p>
     * This method is typically used for debugging or deterministic stepping.
     *
     * @param numSteps the number of steps to execute
     * @param pausableActionEnabled whether the pausable action should run
     * @return {@code true} if all steps completed without error
     */
    boolean makeSteps(int numSteps, boolean pausableActionEnabled);

    /**
     * Executes a single simulation step synchronously.
     *
     * @param pausableActionEnabled whether the pausable action should run
     * @return {@code true} if the step completed without error
     */
    boolean makeOneStep(boolean pausableActionEnabled);
}
