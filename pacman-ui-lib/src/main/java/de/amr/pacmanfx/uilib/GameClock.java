/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
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
            createClockWork();
            if (runningWhenChanged) {
                start();
            }
        }
    };

    private final BooleanProperty paused = new SimpleBooleanProperty(false);

    private final BooleanProperty timeMeasured = new SimpleBooleanProperty(false);

    private Timeline clockWork;
    private Runnable pausableAction = () -> {};
    private Runnable permanentAction = () -> {};

    private long updateCount;
    private long tickCount;

    private long lastTicksPerSec;
    private long countTicksStartTime;
    private long ticksInFrame;

    public GameClock() {
        createClockWork();
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
        clockWork.play();
    }

    public void stop() {
        clockWork.stop();
    }

    public boolean isRunning() {
        return clockWork.getStatus() == Status.RUNNING;
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

    private void createClockWork() {
        Duration period = Duration.seconds(1.0 / targetFrameRate());
        clockWork = new Timeline(targetFrameRate(), new KeyFrame(period, e -> makeOneStep(!isPaused())));
        clockWork.setCycleCount(Animation.INDEFINITE);
        clockWork.statusProperty().addListener((py, oldStatus, newStatus) ->
            Logger.info("{} -> {}, target freq: {} Hz", oldStatus, newStatus, targetFrameRate()));
    }

    private void execute(Runnable action, String logMessage) {
        if (timeMeasured.get()) {
            double start = System.nanoTime();
            action.run();
            double duration = System.nanoTime() - start;
            Logger.info(logMessage, duration / 1e6);
        } else {
            action.run();
        }
    }
}