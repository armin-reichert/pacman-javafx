/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import de.amr.games.pacman.model.GameModel;
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
public class GameClockFX {

    public final DoubleProperty targetFrameRatePy = new SimpleDoubleProperty(this, "targetFrameRate", GameModel.FPS) {
        @Override
        protected void invalidated() {
            handleTargetFrameRateChanged();
        }
    };

    public final BooleanProperty pausedPy = new SimpleBooleanProperty(this, "paused", false);

    public final BooleanProperty timeMeasuredPy = new SimpleBooleanProperty(this, "timeMeasured", false);

    private Runnable pauseableCallback = () -> {};
    private Runnable permanentCallback = () -> {};
    private Timeline timeLine;
    private long updateCount;
    private long tickCount;

    private long ticksPerSec;
    private long countTicksStartTime;
    private long ticksInFrame;

    public GameClockFX() {
        createTimeline(targetFrameRatePy.get());
    }

    public void setPauseableCallback(Runnable callback) {
        this.pauseableCallback = callback;
    }

    public void setPermanentCallback(Runnable callback) {
        this.permanentCallback = callback;
    }

    private void createTimeline(double targetFPS) {
        var tickDuration = Duration.seconds(1.0 / targetFPS);
        timeLine = new Timeline(targetFPS, new KeyFrame(tickDuration, e -> makeStep(!isPaused())));
        timeLine.setCycleCount(Animation.INDEFINITE);
    }

    private void handleTargetFrameRateChanged() {
        boolean running = timeLine.getStatus() == Status.RUNNING;
        if (running) {
            timeLine.stop();
        }
        createTimeline(targetFrameRatePy.get());
        if (running) {
            start();
        }
    }

    public double getTargetFrameRate() {
        return targetFrameRatePy.get();
    }

    public void setTargetFrameRate(double fps) {
        targetFrameRatePy.set(fps);
    }

    public void start() {
        if (timeLine.getStatus() != Status.RUNNING) {
            timeLine.play();
            Logger.info("Clock status: {}", timeLine.getStatus());
        } else {
            Logger.info("Clock status remains {}", timeLine.getStatus());
        }
        Logger.info("Clock target frequency: {} Hz", getTargetFrameRate());
    }

    public void stop() {
        if (timeLine.getStatus() == Status.RUNNING) {
            timeLine.stop();
            Logger.info("Clock status: {}", timeLine.getStatus());
        } else {
            Logger.info("Clock status remains {}", timeLine.getStatus());
        }
    }

    public boolean isRunning() {
        return timeLine.getStatus() == Status.RUNNING;
    }

    public boolean isPaused() {
        return pausedPy.get();
    }

    public double getActualFrameRate() {
        return ticksPerSec;
    }

    /**
     * @return total number of ticks this clock has made.
     */
    public long getTickCount() {
        return tickCount;
    }

    public long getUpdateCount() {
        return updateCount;
    }

    public void makeSteps(int n, boolean pauseableCallbackEnabled) {
        for (int i = 0; i < n; ++i) {
            makeStep(pauseableCallbackEnabled);
        }
    }

    public void makeStep(boolean pauseableCallbackEnabled) {
        long now = System.nanoTime();
        if (pauseableCallbackEnabled) {
            execute(pauseableCallback, "Pauseable phase: {} milliseconds");
            updateCount++;
        }
        execute(permanentCallback, "Continous phase: {} milliseconds");
        ++tickCount;
        ++ticksInFrame;
        computeFrameRate(now);
    }

    private void execute(Runnable callback, String logMessage) {
        if (timeMeasuredPy.get()) {
            double start = System.nanoTime();
            callback.run();
            double duration = System.nanoTime() - start;
            Logger.info(logMessage, duration / 1e6);
        } else {
            callback.run();
        }
    }

    private void computeFrameRate(long time) {
        if (time - countTicksStartTime > 1e9) {
            ticksPerSec = ticksInFrame;
            ticksInFrame = 0;
            countTicksStartTime = time;
        }
    }
}