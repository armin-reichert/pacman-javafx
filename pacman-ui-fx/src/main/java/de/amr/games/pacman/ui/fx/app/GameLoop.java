package de.amr.games.pacman.ui.fx.app;

import de.amr.games.pacman.controller.PacManGameController;
import javafx.animation.AnimationTimer;

class GameLoop extends AnimationTimer {

	public final PacManGameController controller;
//		double lastStepTime, deltaTimeMillis, start, timeConsumed;

	public GameLoop(PacManGameController controller) {
		this.controller = controller;
	}

	@Override
	public void handle(long now) {
//			deltaTimeMillis = (now - lastStepTime) / 1_000_000.0;
//			log("Time since last controller step: %f millis", deltaTimeMillis);
//			start = System.nanoTime();
		controller.step();
//			lastStepTime = now;
//			timeConsumed = (System.nanoTime() - start) / 1e6;
//			log("\tController step: %f millis", timeConsumed);
//			start = System.nanoTime();
		controller.userInterface.update();
//			timeConsumed = (System.nanoTime() - start) / 1e6;
//			log("\tUI update: %f millis", timeConsumed);
	}
}