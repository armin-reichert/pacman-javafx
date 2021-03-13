package de.amr.games.pacman.ui.fx.app;

import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.fx.common.GlobalSettings;
import javafx.animation.AnimationTimer;

class GameLoop extends AnimationTimer {

	public final PacManGameController controller;

	public GameLoop(PacManGameController controller) {
		this.controller = controller;
	}

	@Override
	public void handle(long now) {
		if (!GlobalSettings.paused) {
			if (GlobalSettings.measureTime) {
				measureTime(controller::step, "Controller step");
				measureTime(controller.userInterface::update, "User interface update");
			} else {
				controller.step();
				controller.userInterface.update();
			}
		}
	}

	private void measureTime(Runnable runnable, String description) {
		double start = System.nanoTime();
		runnable.run();
		double duration = (System.nanoTime() - start) / 1e6;
		log("%s took %f millis", description, duration);
	}
}