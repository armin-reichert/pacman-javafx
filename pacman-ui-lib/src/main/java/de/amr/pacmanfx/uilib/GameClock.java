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

/**
 * Game clock with modifiable frame rate.
 */
public class GameClock {

    public static final int DEFAULT_TARGET_FRAME_RATE = 60;

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

    private final BooleanProperty paused = new SimpleBooleanProperty(false);

    private final BooleanProperty timeMeasured = new SimpleBooleanProperty(false);

    private Timeline clockwork;
    private Runnable pausableAction = () -> {};
    private Runnable permanentAction = () -> {};

    private long updateCount;
    private long tickCount;

    private long lastTicksPerSec;
    private long countTicksStartTime;
    private long ticksInFrame;

    private Consumer<Throwable> errorHandler = x -> Logger.error(x, "Game clock encountered error");

    public GameClock() {
        createClockwork(targetFrameRate());
    }

    public void setErrorHandler(Consumer<Throwable> errorHandler) {
        this.errorHandler = errorHandler;
    }

    public void setPausableAction(Runnable action) {
        this.pausableAction = action;
    }

    public void setPermanentAction(Runnable action) {
        this.permanentAction = action;
    }

    public DoubleProperty targetFrameRateProperty() {
        return targetFrameRate;
    }

    public double targetFrameRate() {
        return targetFrameRate.get();
    }

    public void setTargetFrameRate(double fps) {
        targetFrameRate.set(fps);
    }

    public BooleanProperty pausedProperty() { return paused; }

    public void setPaused(boolean b) {
        paused.set(b);
    }

    public boolean isPaused() { return paused.get(); }

    public BooleanProperty timeMeasuredProperty() { return timeMeasured; }

    public void start() {
        setPaused(false);
        clockwork.play();
    }

    public void stop() {
        clockwork.stop();
    }

    public boolean isRunning() {
        return clockwork.getStatus() == Status.RUNNING;
    }

    public double lastTicksPerSecond() {
        return lastTicksPerSec;
    }

    public long tickCount() { return tickCount; }

    public long updateCount() {
        return updateCount;
    }

    public boolean makeSteps(int n, boolean pausableActionEnabled) {
        for (int i = 0; i < n; ++i) {
            boolean success = makeOneStep(pausableActionEnabled);
            if (!success) return false;
        }
        return true;
    }

    public boolean makeOneStep(boolean pausableActionEnabled) {
        final long now = System.nanoTime();
        if (pausableActionEnabled) {
            try {
                execute(pausableAction, "Pausable action took {} milliseconds");
                updateCount++;
            } catch (Throwable x) {
                errorHandler.accept(x);
                return false;
            }
        }
        try {
            execute(permanentAction, "Permanent action took {} milliseconds");
            ++tickCount;
            ++ticksInFrame;
            if (now - countTicksStartTime > 1e9) {
                lastTicksPerSec = ticksInFrame;
                ticksInFrame = 0;
                countTicksStartTime = now;
            }
            return true;
        } catch (Throwable x) {
            errorHandler.accept(x);
            return false;
        }
    }

    private void createClockwork(double frameRate) {
        final var period = Duration.seconds(1.0 / frameRate);
        clockwork = new Timeline(frameRate, new KeyFrame(period, _ -> makeOneStep(!isPaused())));
        clockwork.setCycleCount(Animation.INDEFINITE);
        clockwork.statusProperty().addListener((_, oldStatus, newStatus) ->
            Logger.info("Clock status {} -> {}, target frequency: {} Hz", oldStatus, newStatus, frameRate));
    }

    private void execute(Runnable action, String logMessage) {
        if (timeMeasured.get()) {
            final double start = System.nanoTime();
            action.run();
            final double duration = System.nanoTime() - start;
            Logger.info(logMessage, duration / 1e6);
        } else {
            action.run();
        }
    }
}