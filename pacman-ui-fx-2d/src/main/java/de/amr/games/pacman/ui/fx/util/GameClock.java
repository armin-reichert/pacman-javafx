/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx.util;

import org.tinylog.Logger;

import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;

/**
 * Game clock with modifiable frame rate.
 * 
 * @author Armin Reichert
 */
public abstract class GameClock {

	public final IntegerProperty targetFrameratePy = new SimpleIntegerProperty(this, "targetFramerate", 60) {
		@Override
		protected void invalidated() {
			updateClock();
		}
	};
	public final BooleanProperty pausedPy = new SimpleBooleanProperty(this, "paused", false);
	public final BooleanProperty timeMeasuredPy = new SimpleBooleanProperty(this, "timeMeasured", false);

	private Timeline clock;
	private long updateCount;
	private long ticksPerSec;
	private long countTicksStartTime;
	private long ticks;

	protected GameClock() {
		createClock();
	}

	private void createClock() {
		int fps = targetFrameratePy.get();
		var tickDuration = Duration.millis(1000.0 / fps);
		clock = new Timeline(fps, new KeyFrame(tickDuration, e -> executeSingleStep(!isPaused())));
		clock.setCycleCount(Animation.INDEFINITE);
		Logger.info("Created clock with {} fps", fps);
	}

	private void updateClock() {
		boolean running = clock.getStatus() == Status.RUNNING;
		if (running) {
			clock.stop();
		}
		createClock();
		if (running) {
			start();
		}
	}

	/**
	 * Code called in update phase (e.g. simulation).
	 */
	public abstract void doUpdate();

	/**
	 * Code called after update phase (e.g. rendering).
	 */
	public abstract void doRender();

	public void start() {
		clock.play();
	}

	public void stop() {
		clock.stop();
	}

	public boolean isRunning() {
		return clock.getStatus() == Status.RUNNING;
	}

	public boolean isPaused() {
		return pausedPy.get();
	}

	public long getUpdateCount() {
		return updateCount;
	}

	public long getFPS() {
		return ticksPerSec;
	}

	public void setTimeMeasured(boolean measured) {
		timeMeasuredPy.set(measured);
	}

	public void executeSteps(int n, boolean updateEnabled) {
		for (int i = 0; i < n; ++i) {
			executeSingleStep(updateEnabled);
		}
	}

	public void executeSingleStep(boolean updateEnabled) {
		long tickTime = System.nanoTime();
		if (updateEnabled) {
			runPhase(this::doUpdate, "Update phase: {} milliseconds");
			updateCount++;
		}
		runPhase(this::doRender, "Render phase: {} milliseconds");
		++ticks;
		computeFrameRate(tickTime);
	}

	private void runPhase(Runnable phase, String logMessage) {
		if (timeMeasuredPy.get()) {
			double startNanos = System.nanoTime();
			phase.run();
			double durationNanos = System.nanoTime() - startNanos;
			Logger.info(logMessage, durationNanos / 1e6);
		} else {
			phase.run();
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