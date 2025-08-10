/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
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

/**
 * Game clock with modifiable frame rate.
 *
 * @author Armin Reichert
 */
public class GameClock {

    private final DoubleProperty targetFrameRatePy = new SimpleDoubleProperty(this, "targetFrameRate", 60) {
        @Override
        protected void invalidated() {
            handleTargetFrameRateChanged();
        }
    };

    private final BooleanProperty pausedPy = new SimpleBooleanProperty(this, "paused", false);

    private final BooleanProperty timeMeasuredPy = new SimpleBooleanProperty(this, "timeMeasured", false);

    private Runnable pausableAction = () -> {};
    private Runnable permanentAction = () -> {};
    private Timeline clockWork;
    private long updateCount;
    private long tickCount;

    private long lastTicksPerSec;
    private long countTicksStartTime;
    private long ticksInFrame;

    public GameClock() {
        createClockWork(targetFrameRatePy.get());
    }

    public void setPausableAction(Runnable action) {
        this.pausableAction = action;
    }

    public void setPermanentAction(Runnable action) {
        this.permanentAction = action;
    }

    public DoubleProperty targetFrameRateProperty() {
        return targetFrameRatePy;
    }

    public BooleanProperty pausedProperty() { return pausedPy; }

    public BooleanProperty timeMeasuredProperty() { return timeMeasuredPy; }

    public double targetFrameRate() {
        return targetFrameRatePy.get();
    }

    public void setTargetFrameRate(double fps) {
        targetFrameRatePy.set(fps);
    }

    public void start() {
        pausedProperty().set(false);
        clockWork.play();
    }

    public void stop() {
        clockWork.stop();
    }

    public boolean isRunning() {
        return clockWork.getStatus() == Status.RUNNING;
    }

    public boolean isPaused() { return pausedPy.get(); }

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
        long now = System.nanoTime();
        if (pausableActionEnabled) {
            try {
                execute(pausableAction, "Pausable action took {} milliseconds");
                updateCount++;
            } catch (Throwable x) {
                Logger.error(x);
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
        } catch (Throwable x) {
            Logger.error(x);
            return false;
        }
        return true;
    }

    private void createClockWork(double frameRate) {
        clockWork = new Timeline(frameRate, new KeyFrame(Duration.seconds(1.0 / frameRate), e -> makeOneStep(!isPaused())));
        clockWork.setCycleCount(Animation.INDEFINITE);
        clockWork.statusProperty().addListener((py, oldStatus, newStatus) ->
            Logger.info("{} -> {}, target freq: {} Hz", oldStatus, newStatus, targetFrameRate()));
    }

    private void handleTargetFrameRateChanged() {
        boolean running = clockWork.getStatus() == Status.RUNNING;
        if (running) {
            clockWork.stop();
        }
        createClockWork(targetFrameRatePy.get());
        if (running) {
            start();
        }
    }

    private void execute(Runnable action, String logMessage) {
        if (timeMeasuredPy.get()) {
            double start = System.nanoTime();
            action.run();
            double duration = System.nanoTime() - start;
            Logger.info(logMessage, duration / 1e6);
        } else {
            action.run();
        }
    }
}