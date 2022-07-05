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
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * Game loop with configurable frame rate.
 * 
 * @author Armin Reichert
 */
public class GameLoop {

	private static final Logger logger = LogManager.getFormatterLogger();

	public Runnable update = () -> {
	};

	public Runnable render = () -> {
	};

	private Timeline clock;
	private int totalTicks;
	private int fps;
	private int targetFramerate;
	private long fpsCountStartTime;
	private int frames;
	private boolean timeMeasured;

	public GameLoop(int targetFramerate) {
		setTargetFramerate(targetFramerate);
	}

	public void setTargetFramerate(int fps) {
		targetFramerate = fps;
		boolean restart = false;
		if (clock != null) {
			clock.stop();
			restart = true;
		}
		Duration frameDuration = Duration.millis(1000d / targetFramerate);
		clock = new Timeline(targetFramerate);
		clock.setCycleCount(Animation.INDEFINITE);
		clock.getKeyFrames().add(new KeyFrame(frameDuration, e -> runSingleStep(!Env.paused.get())));
		if (restart) {
			clock.play();
		}
	}

	public void start() {
		clock.play();
		logger.info("Game loop started. Target frame rate: %d", targetFramerate);
	}

	public void stop() {
		clock.stop();
		logger.info("Game loop stopped");
	}

	public int getTargetFramerate() {
		return targetFramerate;
	}

	public int getTotalTicks() {
		return totalTicks;
	}

	public int getFPS() {
		return fps;
	}

	public void setTimeMeasured(boolean timeMeasured) {
		this.timeMeasured = timeMeasured;
	}

	public void runSingleStep(boolean updateEnabled) {
		long now = System.nanoTime();
		if (updateEnabled) {
			run(update, "Update phase: %f milliseconds");
		}
		run(render, "Render phase: %f milliseconds");
		computeFrameRate(now);
	}

	private void computeFrameRate(long now) {
		totalTicks++;
		++frames;
		if (now - fpsCountStartTime > 1e9) {
			fps = frames;
			frames = 0;
			fpsCountStartTime = now;
		}
	}

	private void run(Runnable phase, String message) {
		if (timeMeasured) {
			double startNanos = System.nanoTime();
			phase.run();
			double durationNanos = System.nanoTime() - startNanos;
			logger.info(message, durationNanos / 1e6);
		} else {
			phase.run();
		}
	}
}