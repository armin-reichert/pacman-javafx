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

import java.util.Arrays;
import java.util.List;

import de.amr.games.pacman.ui.fx.Env;
import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Game loop.
 * <p>
 * Note that the animation timer frequency depends on the monitor refresh rate!
 * If your refresh rate is not 60 Hz, the game does not run with intended speed.
 * 
 * @author Armin Reichert
 */
class GameLoop extends AnimationTimer {

	static class GameLoopTask {
		public final String description;
		public final Runnable code;

		public GameLoopTask(String description, Runnable code) {
			this.description = description;
			this.code = code;
		}
	}

	public final IntegerProperty $fps = new SimpleIntegerProperty();
	public final IntegerProperty $totalTicks = new SimpleIntegerProperty();

	private final List<GameLoopTask> taskList;
	private long fpsCountStartTime;
	private int frames;

	public GameLoop(GameLoopTask... tasks) {
		taskList = Arrays.asList(tasks);
	}

	@Override
	public void handle(long now) {
		if ($totalTicks.get() % Env.$slowDown.get() == 0 && !Env.$paused.get()) {
			taskList.forEach(this::runTask);
		}
		++frames;
		if (now - fpsCountStartTime > 1e9) {
			$fps.set(frames);
			frames = 0;
			fpsCountStartTime = now;
		}
		$totalTicks.set($totalTicks.get() + 1);
	}

	private void runTask(GameLoopTask task) {
		if (Env.$isTimeMeasured.get()) {
			double start_ns = System.nanoTime();
			task.code.run();
			double duration_ns = System.nanoTime() - start_ns;
			log("%s: %f milliseconds", task.description, duration_ns / 1e6);
		} else {
			task.code.run();
		}
	}
}