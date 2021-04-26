package de.amr.games.pacman.ui.fx.app;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.LinkedHashMap;
import java.util.Map;

import de.amr.games.pacman.ui.fx.Env;
import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Game loop.
 * <p>
 * Note that the animation timer frequency is influenced from the monitor refresh rate!
 * 
 * @author Armin Reichert
 */
class GameLoop extends AnimationTimer {

	public final IntegerProperty $fps = new SimpleIntegerProperty();
	public final IntegerProperty $totalTicks = new SimpleIntegerProperty();

	private final Map<String, Runnable> tasks = new LinkedHashMap<>();

	private long fpsCountStartTime;
	private int frames;

	@Override
	public void handle(long now) {
		if ($totalTicks.get() % Env.$slowDown.get() == 0) {
			if (!Env.$paused.get()) {
				if (Env.$timeMeasured.get()) {
					for (String name : tasks.keySet()) {
						measureTaskTime(tasks.get(name), name);
					}
				} else {
					tasks.values().forEach(Runnable::run);
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

	public void addTask(String name, Runnable task) {
		tasks.put(name, task);
	}

	private void measureTaskTime(Runnable task, String description) {
		double start = System.nanoTime();
		try {
			task.run();
			double duration = System.nanoTime() - start;
			log("%s took %f millis", description, duration / 1e6);
		} catch (Exception e) {
			log("%s execution not successful");
			e.printStackTrace();
		}
	}
}