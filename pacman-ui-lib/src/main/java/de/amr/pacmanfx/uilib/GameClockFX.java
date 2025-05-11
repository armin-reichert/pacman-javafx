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
public class GameClockFX {

    private final DoubleProperty targetFrameRatePy = new SimpleDoubleProperty(this, "targetFrameRate", 60) {
        @Override
        protected void invalidated() {
            handleTargetFrameRateChanged();
        }
    };

    private final BooleanProperty pausedPy = new SimpleBooleanProperty(this, "paused", false);

    private final BooleanProperty timeMeasuredPy = new SimpleBooleanProperty(this, "timeMeasured", false);

    private Runnable pauseableAction = () -> {};
    private Runnable permanentAction = () -> {};
    private Timeline animation;
    private long updateCount;
    private long tickCount;

    private long ticksPerSec;
    private long countTicksStartTime;
    private long ticksInFrame;

    public GameClockFX() {
        create(targetFrameRatePy.get());
    }

    public void setPauseableAction(Runnable callback) {
        this.pauseableAction = callback;
    }

    public void setPermanentAction(Runnable callback) {
        this.permanentAction = callback;
    }

    public DoubleProperty targetFrameRateProperty() {
        return targetFrameRatePy;
    }

    public BooleanProperty pausedProperty() { return pausedPy; }

    public BooleanProperty timeMeasuredProperty() { return timeMeasuredPy; }

    public double getTargetFrameRate() {
        return targetFrameRatePy.get();
    }

    public void setTargetFrameRate(double fps) {
        targetFrameRatePy.set(fps);
    }

    public void start() {
        animation.play();
    }

    public void stop() {
        animation.stop();
    }

    public boolean isRunning() {
        return animation.getStatus() == Status.RUNNING;
    }

    public boolean isPaused() {
        return pausedPy.get();
    }

    public double getActualFrameRate() {
        return ticksPerSec;
    }

    public long tickCount() { return tickCount; }

    public long updateCount() {
        return updateCount;
    }

    public boolean makeSteps(int n, boolean pauseableActionEnabled) {
        for (int i = 0; i < n; ++i) {
            boolean success = makeOneStep(pauseableActionEnabled);
            if (!success) return false;
        }
        return true;
    }

    public boolean makeOneStep(boolean pauseableActionEnabled) {
        long now = System.nanoTime();
        if (pauseableActionEnabled) {
            try {
                execute(pauseableAction, "Pauseable action took {} milliseconds");
                updateCount++;
            } catch (Throwable x) {
                Logger.error(x);
                Logger.error("Something very bad happened, game clock has been stopped!");
                return false;
            }
        }
        try {
            execute(permanentAction, "Permanent action took {} milliseconds");
            ++tickCount;
            ++ticksInFrame;
            computeFrameRate(now);
        } catch (Throwable x) {
            Logger.error(x);
            Logger.error("Something very bad happened, game clock has been stopped!");
            return false;
        }
        return true;
    }

    private void create(double targetFPS) {
        var tickDuration = Duration.seconds(1.0 / targetFPS);
        animation = new Timeline(targetFPS, new KeyFrame(tickDuration, e -> makeOneStep(!isPaused())));
        animation.setCycleCount(Animation.INDEFINITE);
        animation.statusProperty().addListener((py, ov, nv) -> {
            Logger.info("Clock status: {} -> {}", ov, nv);
            Logger.info("Clock target frequency: {} Hz", getTargetFrameRate());
        });
    }

    private void handleTargetFrameRateChanged() {
        boolean running = animation.getStatus() == Status.RUNNING;
        if (running) {
            animation.stop();
        }
        create(targetFrameRatePy.get());
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

    private void computeFrameRate(long time) {
        if (time - countTicksStartTime > 1e9) {
            ticksPerSec = ticksInFrame;
            ticksInFrame = 0;
            countTicksStartTime = time;
        }
    }
}