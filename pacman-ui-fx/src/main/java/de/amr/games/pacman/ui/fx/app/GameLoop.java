package de.amr.games.pacman.ui.fx.app;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		taskList = new ArrayList<>(Arrays.asList(tasks));
	}

	@Override
	public void handle(long now) {
		if ($totalTicks.get() % Env.$slowDown.get() == 0) {
			if (!Env.$paused.get()) {
				if (Env.$isTimeMeasured.get()) {
					taskList.forEach(this::runInstrumented);
				} else {
					taskList.forEach(task -> task.code.run());
				}
			}
			++frames;
			if (now - fpsCountStartTime > 1e9) {
				$fps.set(frames);
				frames = 0;
				fpsCountStartTime = now;
			}
		}
		$totalTicks.set($totalTicks.get() + 1);
	}

	private void runInstrumented(GameLoopTask task) {
		try {
			double start_ns = System.nanoTime();
			task.code.run();
			double duration_ns = System.nanoTime() - start_ns;
			log("%s: %f milliseconds", task.description, duration_ns / 1e6);
		} catch (Exception e) {
			log("%s: failed");
			e.printStackTrace();
		}
	}
}