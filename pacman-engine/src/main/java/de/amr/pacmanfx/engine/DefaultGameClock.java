/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.engine;

import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.Validations;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
public class DefaultGameClock implements GameClock {

    public static final int DEFAULT_FRAME_RATE = 60;

    /**
     * The desired frame rate. Changing this property rebuilds the internal
     * {@link Timeline}. If the clock was running, it is stopped and restarted
     * with the new timing.
     */
    private final IntegerProperty targetFrameRate = new SimpleIntegerProperty(DEFAULT_FRAME_RATE) {
        @Override
        protected void invalidated() {
            final boolean runningWhenChanged = isRunning();
            if (runningWhenChanged) {
                stop();
            }
            rebuildTimeline();
            if (runningWhenChanged) {
                start();
            }
        }
    };

    /** Indicates whether the clock currently processes the "update" action. */
    private final BooleanProperty updatesDisabled = new SimpleBooleanProperty(false);

    /**
     * Enables or disables time‑measurement logging. When enabled, each action
     * logs its execution time in milliseconds.
     */
    private final BooleanProperty timeMeasured = new SimpleBooleanProperty(false);

    /** The JavaFX timeline that drives periodic tick execution. */
    private final Timeline timeline = new Timeline();

    /** Action executed only when the clock allows updates. */
    private Runnable pausableAction = () -> {};

    /** Action executed on every tick, regardless of pause state. */
    private Runnable permanentAction = () -> {};

    private long pausableActionCount;

    private long ticksTotal;

    private long fps;

    private long countStartNanos;

    private long ticksInFrame;

    /** Handler invoked when an exception occurs during tick execution. */
    private Consumer<Throwable> errorHandler = x -> Logger.error(x, "Game clock encountered error");

    /**
     * Creates a new game clock with the default target frame rate.
     * The internal {@link Timeline} is initialized immediately.
     */
    public DefaultGameClock() {
        rebuildTimeline();
        timeline.statusProperty().addListener((_, oldStatus, newStatus) ->
            Logger.info("Clock status {} -> {}, target FPS: {} Hz", oldStatus, newStatus, targetFrameRate()));
    }

    @Override
    public void setErrorHandler(Consumer<Throwable> errorHandler) {
        this.errorHandler = requireNonNull(errorHandler);
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
    public IntegerProperty targetFrameRateProperty() {
        return targetFrameRate;
    }

    @Override
    public int targetFrameRate() {
        return targetFrameRate.get();
    }

    @Override
    public void setTargetFrameRate(int fps) {
        targetFrameRate.set(fps);
    }

    @Override
    public BooleanProperty updatesDisabledProperty() { return updatesDisabled; }

    @Override
    public void setUpdatesDisabled(boolean b) {
        updatesDisabled.set(b);
    }

    @Override
    public boolean areUpdatesDisabled() { return updatesDisabled.get(); }

    @Override
    public BooleanProperty timeMeasuredProperty() { return timeMeasured; }

    @Override
    public void start() {
        timeline.play();
        Logger.info("Game clock started! Target FPS: {}", targetFrameRate());
    }

    @Override
    public void stop() {
        timeline.stop();
        Logger.info("Game clock stopped!");
    }

    @Override
    public boolean isRunning() {
        return timeline.getStatus() == Status.RUNNING;
    }

    @Override
    public double fps() {
        return fps;
    }

    @Override
    public long currentTick() { return ticksTotal; }

    @Override
    public long pausableActionCount() {
        return pausableActionCount;
    }

    @Override
    public boolean makeSteps(int numSteps, boolean pausableActionEnabled) {
        Validations.requireNonNegative(numSteps);
        for (int i = 0; i < numSteps; ++i) {
            if (!makeStep(pausableActionEnabled)) return false;
        }
        return true;
    }

    @Override
    public boolean makeStep(boolean pausableActionEnabled) {
        try {
            if (pausableActionEnabled) {
                runAction(pausableAction, "Pausable action took {} milliseconds");
                pausableActionCount++;
            }
            runAction(permanentAction, "Permanent action took {} milliseconds");
            computeFPS();
        } catch (Throwable x) {
            errorHandler.accept(x);
            return false;
        }
        return true;
    }

    private void computeFPS() {
        final long frameStartNanos = System.nanoTime();
        ++ticksTotal;
        ++ticksInFrame;
        if (frameStartNanos - countStartNanos > 1e9) {
            fps = ticksInFrame;
            ticksInFrame = 0;
            countStartNanos = frameStartNanos;
        }
    }

    /**
     * Rebuilds the internal {@link Timeline} for the given frame rate.
     * <p>
     * The timeline is configured with a single {@link KeyFrame} whose duration
     * corresponds to the desired frame period. Each frame triggers a call to
     * {@link #makeStep(boolean)}.
     */
    private void rebuildTimeline() {
        final Duration targetDuration = Duration.seconds(1.0 / targetFrameRate());
        timeline.getKeyFrames().setAll(new KeyFrame(targetDuration, _ -> makeStep(!areUpdatesDisabled())));
        timeline.setCycleCount(Animation.INDEFINITE);
        ticksInFrame = 0;
        countStartNanos = System.nanoTime();
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
    private void runAction(Runnable action, String logMessage) {
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
