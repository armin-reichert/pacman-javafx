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

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.animation.Animation;
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
public class GameLoop {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	public final IntegerProperty targetFrameratePy = new SimpleIntegerProperty(60);
	public final BooleanProperty pausedPy = new SimpleBooleanProperty(false);
	public final BooleanProperty measuredPy = new SimpleBooleanProperty(false);

	private Timeline clock;
	private long totalTicks;
	private long fps;
	private long fpsCountStartTime;
	private long frames;
	private Runnable updateTask = () -> {
	};
	private Runnable renderTask = () -> {
	};

	public GameLoop(int targetFramerate) {
		targetFrameratePy.set(targetFramerate);
		targetFrameratePy.addListener((x, y, newFramerate) -> updateClock(newFramerate.intValue()));
		updateClock(targetFramerate);
	}

	private void updateClock(int fps) {
		boolean restart = false;
		if (clock != null) {
			clock.stop();
			restart = true;
		}
		Duration frameDuration = Duration.millis(1000d / fps);
		clock = new Timeline(fps, new KeyFrame(frameDuration, e -> makeOneStep(!isPaused())));
		clock.setCycleCount(Animation.INDEFINITE);
		if (restart) {
			clock.play();
		}
	}

	public void setTargetFramerate(int fps) {
		targetFrameratePy.set(fps);
	}

	public int getTargetFramerate() {
		return targetFrameratePy.get();
	}

	public void setUpdateTask(Runnable updateTask) {
		this.updateTask = Objects.requireNonNull(updateTask);
	}

	public void setRenderTask(Runnable renderTask) {
		this.renderTask = Objects.requireNonNull(renderTask);
	}

	public boolean isPaused() {
		return pausedPy.get();
	}

	public void start() {
		clock.play();
		LOGGER.info("Game loop started. Target frame rate: %d", getTargetFramerate());
	}

	public void stop() {
		clock.stop();
		LOGGER.info("Game loop stopped");
	}

	public long getTotalTicks() {
		return totalTicks;
	}

	public long getFPS() {
		return fps;
	}

	public void setTimeMeasured(boolean measured) {
		measuredPy.set(measured);
	}

	public void makeOneStep(boolean updateEnabled) {
		long tickTime = System.nanoTime();
		if (updateEnabled) {
			run(updateTask, "Update phase: %f milliseconds");
		}
		run(renderTask, "Render phase: %f milliseconds");
		totalTicks++;
		++frames;
		computeFrameRate(tickTime);
	}

	private void run(Runnable phase, String message) {
		if (measuredPy.get()) {
			double startNanos = System.nanoTime();
			phase.run();
			double durationNanos = System.nanoTime() - startNanos;
			LOGGER.info(message, durationNanos / 1e6);
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