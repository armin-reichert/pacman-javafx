/*
MIT License

Copyright (c) 2021 Armin Reichert

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

import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.ui.fx.Env;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;

/**
 * Game loop with configurable frame rate.
 * 
 * @author Armin Reichert
 */
public class GameLoop {

	public final IntegerProperty $fps = new SimpleIntegerProperty();
	public final IntegerProperty $totalTicks = new SimpleIntegerProperty();

	private Timeline tl;
	private int targetFrameRate = 60;
	private Runnable update;
	private Runnable render;
	private long fpsCountStartTime;
	private int frames;

	public GameLoop(Runnable update, Runnable render) {
		this.update = update;
		this.render = render;
		setTargetFrameRate(60);
	}

	public int getTargetFrameRate() {
		return targetFrameRate;
	}

	public void setTargetFrameRate(int fps) {
		targetFrameRate = fps;
		boolean restart = false;
		if (tl != null) {
			tl.stop();
			restart = true;
		}
		tl = new Timeline(targetFrameRate);
		tl.setCycleCount(Animation.INDEFINITE);
		tl.getKeyFrames().add(new KeyFrame(Duration.millis(1000d / targetFrameRate), e -> runSingleFrame()));
		if (restart) {
			tl.play();
		}
	}

	public void start() {
		tl.play();
		log("Game loop started. Target frame rate: %d", targetFrameRate);
	}

	public void stop() {
		tl.stop();
		log("Game loop stopped");
	}

	private void runSingleFrame() {
		long now = System.nanoTime();
		if (!Env.$paused.get()) {
			runUpdate(now);
		}
		// Note: we must also render at 60Hz because some animations depend on the rendering speed
		render.run();
		$totalTicks.set($totalTicks.get() + 1);
		++frames;
		if (now - fpsCountStartTime > 1e9) {
			$fps.set(frames);
			frames = 0;
			fpsCountStartTime = now;
		}
	}

	private void runUpdate(long updateTime) {
		if (Env.$isTimeMeasured.get()) {
			double start_ns = System.nanoTime();
			update.run();
			double duration_ns = System.nanoTime() - start_ns;
			log("Update took %f milliseconds", duration_ns / 1e6);
		} else {
			update.run();
		}
	}
}