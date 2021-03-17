package de.amr.games.pacman.ui.fx.app;

import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX;
import de.amr.games.pacman.ui.fx.common.Env;
import javafx.animation.AnimationTimer;

class GameLoop extends AnimationTimer {

	public final PacManGameController controller;
	public final PacManGameUI_JavaFX userInterface;

	public GameLoop(PacManGameController controller, PacManGameUI_JavaFX userInterface) {
		this.controller = controller;
		this.userInterface = userInterface;

	}

	@Override
	public void handle(long now) {
		if (!Env.$paused.get()) {
			if (Env.$measureTime.get()) {
				measureTime(controller::step, "Controller step");
				measureTime(userInterface::update, "User interface update");
			} else {
				controller.step();
				userInterface.update();
				userInterface.updateInfoView();
			}
		} else {
			userInterface.updateInfoView();
		}
	}

	private void measureTime(Runnable runnable, String description) {
		double start = System.nanoTime();
		runnable.run();
		double duration = (System.nanoTime() - start) / 1e6;
		log("%s took %f millis", description, duration);
	}
}