package de.amr.games.pacman.ui.fx.app;

import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX;
import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Game loop.
 * <p>
 * Note that the animation timer frequency is determined from the monitor refresh rate!
 * 
 * @author Armin Reichert
 */
class GameLoop extends AnimationTimer {

	public IntegerProperty $fps = new SimpleIntegerProperty();

	private final PacManGameController controller;
	private final PacManGameUI_JavaFX userInterface;

	private long totalTicks;
	private long fpsCountStartTime;
	private int frames;

	public GameLoop(PacManGameController controller, PacManGameUI_JavaFX userInterface) {
		this.controller = controller;
		this.userInterface = userInterface;
	}

	public int getFPS() {
		return $fps.get();
	}

	@Override
	public void handle(long now) {
		if (Env.$paused.get()) {
			userInterface.hud.update();
		} else {
			if (totalTicks % Env.$slowdown.get() == 0) {
				if (Env.$measureTime.get()) {
					measureTime(controller::step, "Controller step");
					measureTime(userInterface::update, "User interface update");
				} else {
					controller.step();
					userInterface.update();
				}
				++frames;
				if (now - fpsCountStartTime > 1e9) {
					$fps.set(frames);
					frames = 0;
					fpsCountStartTime = now;
					userInterface.setTitle(String.format("Pac-Man / Ms. Pac-Man (%d fps, JavaFX)", getFPS()));
				}
			}
			++totalTicks;
		}
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