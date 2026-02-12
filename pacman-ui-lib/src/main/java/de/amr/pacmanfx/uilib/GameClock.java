/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib;

import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * A configurable game clock that drives the simulation at a target frame rate.
 *
 * <p>The clock is implemented using a JavaFX {@link Timeline}. Each frame triggers
 * a simulation “tick”, during which two types of actions may run:</p>
 *
 * <ul>
 *   <li><strong>Pausable action</strong> – executed only when the clock is not paused</li>
 *   <li><strong>Permanent action</strong> – executed every tick regardless of pause state</li>
 * </ul>
 *
 * <p>The target frame rate can be changed at runtime. When modified, the clock
 * automatically rebuilds its internal {@code Timeline} and resumes running if it
 * was active before the change.</p>
 *
 * <p>The clock also provides optional time‑measurement logging, tick counters,
 * and a customizable error handler for exceptions thrown during tick execution.</p>
 */
public class GameClock {

    /** Default target frame rate in frames per second. */
    public static final int DEFAULT_TARGET_FRAME_RATE = 60;

    /**
     * The desired frame rate. Changing this property rebuilds the internal
     * {@link Timeline}. If the clock was running, it is stopped and restarted
     * with the new timing.
     */
    private final DoubleProperty targetFrameRate = new SimpleDoubleProperty(DEFAULT_TARGET_FRAME_RATE) {
        @Override
        protected void invalidated() {
            boolean runningWhenChanged = isRunning();
            if (runningWhenChanged) {
                stop();
            }
            createClockwork(targetFrameRate());
            if (runningWhenChanged) {
                start();
            }
        }
    };

    /** Indicates whether the clock is currently paused. */
    private final BooleanProperty paused = new SimpleBooleanProperty(false);

    /**
     * Enables or disables time‑measurement logging. When enabled, each action
     * logs its execution time in milliseconds.
     */
    private final BooleanProperty timeMeasured = new SimpleBooleanProperty(false);

    private final Timeline clockwork = new Timeline();
    private Runnable pausableAction = () -> {};
    private Runnable permanentAction = () -> {};

    private long pausableUpdatesCount;
    private long tickCount;

    private long lastTicksPerSec;
    private long countTicksStartTime;
    private long ticksInFrame;

    /** Handler invoked when an exception occurs during tick execution. */
    private Consumer<Throwable> errorHandler = x -> Logger.error(x, "Game clock encountered error");

    /**
     * Creates a new game clock with the default target frame rate.
     */
    public GameClock() {
        createClockwork(targetFrameRate());
        clockwork.statusProperty().addListener((_, oldStatus, newStatus) ->
            Logger.info("Clock status {} -> {}, target frequency: {} Hz", oldStatus, newStatus, targetFrameRate()));
    }

    /**
     * Sets the error handler invoked when a tick action throws an exception.
     *
     * @param errorHandler the handler to use
     */
    public void setErrorHandler(Consumer<Throwable> errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * Sets the action executed only when the clock is not paused.
     *
     * @param action the pausable action
     */
    public void setPausableAction(Runnable action) {
        this.pausableAction = requireNonNull(action);
    }

    /**
     * Sets the action executed every tick, regardless of pause state.
     *
     * @param action the permanent action
     */
    public void setPermanentAction(Runnable action) {
        this.permanentAction = requireNonNull(action);
    }

    /** @return the property representing the target frame rate */
    public DoubleProperty targetFrameRateProperty() {
        return targetFrameRate;
    }

    /** @return the current target frame rate in frames per second */
    public double targetFrameRate() {
        return targetFrameRate.get();
    }

    /**
     * Sets the target frame rate. The clock is rebuilt automatically.
     *
     * @param fps the desired frames per second
     */
    public void setTargetFrameRate(double fps) {
        targetFrameRate.set(fps);
    }

    /** @return the paused property */
    public BooleanProperty pausedProperty() { return paused; }

    /** Sets whether the clock is paused. */
    public void setPaused(boolean b) {
        paused.set(b);
    }

    /** @return {@code true} if the clock is paused */
    public boolean isPaused() { return paused.get(); }

    /** @return the property controlling time‑measurement logging */
    public BooleanProperty timeMeasuredProperty() { return timeMeasured; }

    /**
     * Starts the clock. If paused, the clock is unpaused.
     */
    public void start() {
        setPaused(false);
        clockwork.play();
    }

    /**
     * Stops the clock. No further ticks occur until {@link #start()} is called.
     */
    public void stop() {
        clockwork.stop();
    }

    /**
     * @return {@code true} if the clock is currently running
     */
    public boolean isRunning() {
        return clockwork.getStatus() == Status.RUNNING;
    }

    /**
     * Returns the number of ticks executed during the last measured second.
     *
     * @return ticks per second
     */
    public double lastTicksPerSecond() {
        return lastTicksPerSec;
    }

    /**
     * @return the total number of ticks executed since the clock was created
     */
    public long tickCount() { return tickCount; }

    /**
     * @return the number of pausable updates executed
     */
    public long updateCount() {
        return pausableUpdatesCount;
    }

    /**
     * Executes a fixed number of simulation steps.
     *
     * @param n number of steps
     * @param pausableActionEnabled whether the pausable action should run
     * @return {@code true} if all steps completed successfully
     */
    public boolean makeSteps(int n, boolean pausableActionEnabled) {
        for (int i = 0; i < n; ++i) {
            if (!makeOneStep(pausableActionEnabled)) return false;
        }
        return true;
    }

    /**
     * Executes a single simulation step.
     *
     * @param pausableActionEnabled whether the pausable action should run
     * @return {@code true} if the step completed successfully
     */
    public boolean makeOneStep(boolean pausableActionEnabled) {
        if (pausableActionEnabled) {
            try {
                execute(pausableAction, "Pausable action took {} milliseconds");
                pausableUpdatesCount++;
            } catch (Throwable x) {
                errorHandler.accept(x);
                return false;
            }
        }
        try {
            execute(permanentAction, "Permanent action took {} milliseconds");
            ++tickCount;
            ++ticksInFrame;
            long after = System.nanoTime();
            if (after - countTicksStartTime > 1e9) {
                lastTicksPerSec = ticksInFrame;
                ticksInFrame = 0;
                countTicksStartTime = after;
            }
            return true;
        } catch (Throwable x) {
            errorHandler.accept(x);
            return false;
        }
    }

    /**
     * Rebuilds the internal {@link Timeline} for the given frame rate.
     *
     * @param frameRate the desired frames per second
     */
    private void createClockwork(double frameRate) {
        final var period = Duration.seconds(1.0 / frameRate);
        clockwork.getKeyFrames().setAll(new KeyFrame(period, _ -> makeOneStep(!isPaused())));
        clockwork.setCycleCount(Animation.INDEFINITE);
        ticksInFrame = 0;
        countTicksStartTime = System.nanoTime();
    }

    /**
     * Executes the given action, optionally measuring and logging its duration.
     *
     * @param action the action to run
     * @param logMessage the log message used when time measurement is enabled
     */
    private void execute(Runnable action, String logMessage) {
        if (timeMeasured.get()) {
            final long start = System.nanoTime();
            action.run();
            final long duration = System.nanoTime() - start;
            Logger.info(logMessage, duration / 1e6);
        } else {
            action.run();
        }
    }
}
