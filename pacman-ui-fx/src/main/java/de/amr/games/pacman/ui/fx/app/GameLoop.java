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
import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Game loop.
 * <p>
 * Note that the animation timer frequency depends on the monitor refresh rate! If your refresh rate
 * is not 60 Hz, the game does not run with intended speed.
 * 
 * @author Armin Reichert
 */
public class GameLoop extends AnimationTimer {

	public final IntegerProperty $fps = new SimpleIntegerProperty();
	public final IntegerProperty $totalTicks = new SimpleIntegerProperty();

	private Runnable update;
	private Runnable render;
	private long fpsCountStartTime;
	private int frames;
	private long lastUpdate;

	public GameLoop(Runnable update, Runnable render) {
		this.update = update;
		this.render = render;
	}

	@Override
	public void handle(long now) {
		$totalTicks.set($totalTicks.get() + 1);
		boolean readyForUpdate = now - lastUpdate >= 1e9 / 66; // TODO this is somewhat dubios
		if (readyForUpdate && !Env.$paused.get() && $totalTicks.get() % Env.$slowDown.get() == 0) {
			runUpdate(now);
		}
		++frames;
		if (now - fpsCountStartTime > 1e9) {
			$fps.set(frames);
			frames = 0;
			fpsCountStartTime = now;
		}
		render.run();
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
		lastUpdate = updateTime;
	}
}