/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import de.amr.games.pacman.controller.GameClock;
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
public class GameClockFX implements GameClock {

    private final DoubleProperty targetFrameRatePy = new SimpleDoubleProperty(this, "targetFrameRate", GameModel.FPS) {
        @Override
        protected void invalidated() {
            handleTargetFrameRateChanged();
        }
    };

    public final BooleanProperty pausedPy = new SimpleBooleanProperty(this, "paused", false);

    public final BooleanProperty timeMeasuredPy = new SimpleBooleanProperty(this, "timeMeasured", false);

    private Runnable pauseableCallback = () -> {};
    private Runnable permanentCallback = () -> {};
    private Timeline timeline;
    private long updateCount;
    private long ticksPerSec;
    private long countTicksStartTime;
    private long ticks;

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
        var tick = new KeyFrame(Duration.seconds(1.0 / targetFPS), e -> makeStep(!isPaused()));
        timeline = new Timeline(targetFPS, tick);
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    private void handleTargetFrameRateChanged() {
        boolean running = timeline.getStatus() == Status.RUNNING;
        if (running) {
            timeline.stop();
        }
        createTimeline(targetFrameRatePy.get());
        if (running) {
            start();
        }
    }

    @Override
    public double getTargetFrameRate() {
        return targetFrameRatePy.get();
    }

    @Override
    public void setTargetFrameRate(double fps) {
        targetFrameRatePy.set(fps);
    }

    @Override
    public void start() {
        timeline.play();
        Logger.info("Clock started, target freq: {} Hz", getTargetFrameRate());
    }

    @Override
    public void stop() {
        timeline.stop();
        Logger.info("Clock stopped");
    }

    @Override
    public boolean isRunning() {
        return timeline.getStatus() == Status.RUNNING;
    }

    @Override
    public boolean isPaused() {
        return pausedPy.get();
    }

    @Override
    public double getActualFrameRate() {
        return ticksPerSec;
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
        long executionTime = System.nanoTime();
        if (pauseableCallbackEnabled) {
            execute(pauseableCallback, "Pausable phase: {} milliseconds");
            updateCount++;
        }
        execute(permanentCallback, "Continous phase: {} milliseconds");
        ++ticks;
        computeFrameRate(executionTime);
    }

    private void execute(Runnable callback, String callbackMsg) {
        if (timeMeasuredPy.get()) {
            double start = System.nanoTime();
            callback.run();
            double duration = System.nanoTime() - start;
            Logger.info(callbackMsg, duration / 1e6);
        } else {
            callback.run();
        }
    }

    private void computeFrameRate(long time) {
        if (time - countTicksStartTime > 1e9) {
            ticksPerSec = ticks;
            ticks = 0;
            countTicksStartTime = time;
        }
    }
}