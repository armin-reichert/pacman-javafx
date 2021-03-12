package de.amr.games.pacman.ui.fx.app;

import java.io.IOException;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * The Pac-Man game app running in a JavaFX UI.
 * 
 * @author Armin Reichert
 */
public class PacManGameAppFX extends Application {

	private static CommandLineArgs options;

	private static class GameLoop extends AnimationTimer {

		PacManGameController controller;
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

	public static void main(String[] args) {
		options = new CommandLineArgs(args);
		launch(args);
	}

	@Override
	public void start(Stage stage) throws IOException {
		PacManGameController controller = new PacManGameController(options.pacman ? GameType.PACMAN : GameType.MS_PACMAN);
		PacManGameUI_JavaFX ui = new PacManGameUI_JavaFX(stage, controller, options.height);
		controller.setUserInterface(ui);
		new GameLoop(controller).start();
	}
}