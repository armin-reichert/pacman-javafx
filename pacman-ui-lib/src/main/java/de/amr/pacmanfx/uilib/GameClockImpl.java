/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib;

import de.amr.pacmanfx.GameClock;
import de.amr.pacmanfx.Validations;
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
 * JavaFX-based implementation of {@link GameClock} using a {@link Timeline} to
 * generate periodic ticks at a configurable frame rate.
 * <p>
 * The clock executes two independent actions on each tick:
 * <ul>
 *   <li>a <em>permanent action</em>, executed unconditionally</li>
 *   <li>a <em>pausable action</em>, executed only when the clock is not paused</li>
 * </ul>
 * The clock supports dynamic frame-rate changes, time‑measurement logging,
 * synchronous stepping for debugging, and basic tick statistics.
 * <p>
 * All tick execution occurs on the JavaFX Application Thread, as required by
 * {@link Timeline}. Actions should therefore avoid long‑running work.
 */
public class GameClockImpl implements GameClock {

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

    /** The JavaFX timeline that drives periodic tick execution. */
    private final Timeline clockwork = new Timeline();

    /** Action executed only when the clock is not paused. */
    private Runnable pausableAction = () -> {};

    /** Action executed on every tick, regardless of pause state. */
    private Runnable permanentAction = () -> {};

    private long pausableUpdatesCount;
    private long tickCount;

    private long fps;
    private long countTicksStartTime;
    private long ticksInFrame;

    /** Handler invoked when an exception occurs during tick execution. */
    private Consumer<Throwable> errorHandler = x -> Logger.error(x, "Game clock encountered error");

    /**
     * Creates a new game clock with the default target frame rate.
     * The internal {@link Timeline} is initialized immediately.
     */
    public GameClockImpl() {
        createClockwork(targetFrameRate());
        clockwork.statusProperty().addListener((_, oldStatus, newStatus) ->
            Logger.info("Clock status {} -> {}, target frequency: {} Hz", oldStatus, newStatus, targetFrameRate()));
    }

    @Override
    public void setErrorHandler(Consumer<Throwable> errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public void setPausableAction(Runnable action) {
        this.pausableAction = requireNonNull(action);
    }

    @Override
    public void setPermanentAction(Runnable action) {
        this.permanentAction = requireNonNull(action);
    }

    @Override
    public DoubleProperty targetFrameRateProperty() {
        return targetFrameRate;
    }

    @Override
    public double targetFrameRate() {
        return targetFrameRate.get();
    }

    @Override
    public void setTargetFrameRate(double fps) {
        targetFrameRate.set(fps);
    }

    @Override
    public BooleanProperty pausedProperty() { return paused; }

    @Override
    public void setPaused(boolean b) {
        paused.set(b);
    }

    @Override
    public boolean isPaused() { return paused.get(); }

    @Override
    public BooleanProperty timeMeasuredProperty() { return timeMeasured; }

    @Override
    public void start() {
        setPaused(false);
        clockwork.play();
    }

    @Override
    public void stop() {
        clockwork.stop();
    }

    @Override
    public boolean isRunning() {
        return clockwork.getStatus() == Status.RUNNING;
    }

    @Override
    public double fps() {
        return fps;
    }

    @Override
    public long tickCount() { return tickCount; }

    @Override
    public long pausableUpdatesCount() {
        return pausableUpdatesCount;
    }

    @Override
    public boolean makeSteps(int numSteps, boolean pausableActionIncluded) {
        Validations.requireNonNegative(numSteps);
        for (int i = 0; i < numSteps; ++i) {
            if (!makeOneStep(pausableActionIncluded)) return false;
        }
        return true;
    }

    @Override
    public boolean makeOneStep(boolean pausableActionIncluded) {
        try {
            if (pausableActionIncluded) {
                execute(pausableAction, "Pausable action took {} milliseconds");
                pausableUpdatesCount++;
            }
            execute(permanentAction, "Permanent action took {} milliseconds");
            computeFPS();
        } catch (Throwable x) {
            errorHandler.accept(x);
            return false;
        }
        return true;
    }

    private void computeFPS() {
        final long now = System.nanoTime();
        ++tickCount;
        ++ticksInFrame;
        if (now - countTicksStartTime > 1e9) {
            fps = ticksInFrame;
            ticksInFrame = 0;
            countTicksStartTime = now;
        }
    }

    /**
     * Rebuilds the internal {@link Timeline} for the given frame rate.
     * <p>
     * The timeline is configured with a single {@link KeyFrame} whose duration
     * corresponds to the desired frame period. Each frame triggers a call to
     * {@link #makeOneStep(boolean)}.
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
     * <p>
     * When time measurement is enabled, the execution time is logged in
     * milliseconds using the provided log message.
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
