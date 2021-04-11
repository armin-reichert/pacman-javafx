package de.amr.games.pacman.ui.fx.app;

import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.ui.fx.Env;
import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Game loop.
 * <p>
 * Note that the animation timer frequency is taken from the monitor refresh rate!
 * 
 * @author Armin Reichert
 */
class GameLoop extends AnimationTimer {

	public IntegerProperty $fps = new SimpleIntegerProperty();
	public IntegerProperty $totalTicks = new SimpleIntegerProperty();

	private final Runnable step;
	private final Runnable uiUpdate;

	private long fpsCountStartTime;
	private int frames;

	public GameLoop(Runnable step, Runnable uiUpdate) {
		this.step = step;
		this.uiUpdate = uiUpdate;
	}

	@Override
	public void handle(long now) {
		if ($totalTicks.get() % Env.$slowDown.get() == 0) {
			if (!Env.$paused.get()) {
				if (Env.$timeMeasured.get()) {
					measureTime(step::run, "Controller update");
					measureTime(uiUpdate::run, "User interface update");
				} else {
					step.run();
					uiUpdate.run();
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

	private void measureTime(Runnable runnable, String description) {
		double start = System.nanoTime();
		try {
			runnable.run();
			double duration = (System.nanoTime() - start) / 1e6;
			log("%s took %f millis", description, duration);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}