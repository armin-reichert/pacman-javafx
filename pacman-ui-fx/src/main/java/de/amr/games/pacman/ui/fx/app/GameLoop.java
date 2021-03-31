package de.amr.games.pacman.ui.fx.app;

import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX;
import javafx.animation.AnimationTimer;

class GameLoop extends AnimationTimer {

	final PacManGameController controller;
	final PacManGameUI_JavaFX userInterface;

	public GameLoop(PacManGameController controller, PacManGameUI_JavaFX userInterface) {
		this.controller = controller;
		this.userInterface = userInterface;
	}

	@Override
	public void handle(long now) {
		if (Env.$paused.get()) {
			userInterface.hud.update();
		} else {
			if (Env.$measureTime.get()) {
				measureTime(controller::step, "Controller step");
				measureTime(userInterface::update, "User interface update");
			} else {
				controller.step();
				userInterface.update();
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