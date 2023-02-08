/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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
package de.amr.games.pacman.ui.fx.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
 * Game loop with modifiable frame rate.
 * 
 * @author Armin Reichert
 */
public abstract class GameLoop {

	private static final Logger LOG = LogManager.getFormatterLogger();

	public final IntegerProperty targetFrameratePy = new SimpleIntegerProperty(this, "targetFramerate", 60);
	public final BooleanProperty pausedPy = new SimpleBooleanProperty(this, "paused", false);
	public final BooleanProperty measuredPy = new SimpleBooleanProperty(this, "measured", false);

	private Timeline frameGenerator;

	private long updateCount;
	private long fps;
	private long fpsCountStartTime;
	private long frames;

	protected GameLoop(int targetFramerate) {
		targetFrameratePy.set(targetFramerate);
		targetFrameratePy.addListener((py, oldValue, newValue) -> createFrameGenerator(newValue.intValue()));
		createFrameGenerator(targetFramerate);
	}

	private void createFrameGenerator(int fps) {
		var frameDuration = Duration.millis(1000.0 / fps);
		boolean wasRunning = frameGenerator != null && frameGenerator.getStatus() == Status.RUNNING;
		if (wasRunning) {
			frameGenerator.stop();
		}
		frameGenerator = new Timeline(fps, new KeyFrame(frameDuration, e -> executeSingleStep(!isPaused())));
		frameGenerator.setCycleCount(Animation.INDEFINITE);
		if (wasRunning) {
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
		frameGenerator.play();
	}

	public void stop() {
		frameGenerator.stop();
	}

	public boolean isRunning() {
		return frameGenerator.getStatus() == Status.RUNNING;
	}

	public boolean isPaused() {
		return pausedPy.get();
	}

	public long getUpdateCount() {
		return updateCount;
	}

	public long getFPS() {
		return fps;
	}

	public void setTimeMeasured(boolean measured) {
		measuredPy.set(measured);
	}

	public void executeSteps(int n, boolean updateEnabled) {
		for (int i = 0; i < n; ++i) {
			executeSingleStep(updateEnabled);
		}
	}

	public void executeSingleStep(boolean updateEnabled) {
		long tickTime = System.nanoTime();
		if (updateEnabled) {
			runPhase(this::doUpdate, "Update phase: %f milliseconds");
			updateCount++;
		}
		runPhase(this::doRender, "Render phase: %f milliseconds");
		++frames;
		computeFrameRate(tickTime);
	}

	private void runPhase(Runnable phase, String message) {
		if (measuredPy.get()) {
			double startNanos = System.nanoTime();
			phase.run();
			double durationNanos = System.nanoTime() - startNanos;
			LOG.info(message, durationNanos / 1e6);
		} else {
			phase.run();
		}
	}

	private void computeFrameRate(long time) {
		if (time - fpsCountStartTime > 1e9) {
			fps = frames;
			frames = 0;
			fpsCountStartTime = time;
		}
	}
}