package de.amr.games.pacman.ui.fx.app;

import java.io.IOException;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * The Pac-Man game app running in a JavaFX UI.
 * 
 * @author Armin Reichert
 */
public class PacManGameAppFX extends Application {

	private static Options options;
	private PacManGameController gameController;
	private GameLoop gameLoop;

	public static void main(String[] args) {
		options = new Options(args);
		launch(args);
	}

	@Override
	public void init() throws Exception {
		gameController = new PacManGameController();
		gameController.play(options.gameVariant);
	}

	@Override
	public void start(Stage stage) throws IOException {
		PacManGameUI_JavaFX ui = new PacManGameUI_JavaFX(stage, gameController, options.height);
		gameController.setUI(ui);
		gameLoop = new GameLoop(gameController::step, ui::update);
		gameLoop.start();
		ui.$totalTicks.bind(gameLoop.$totalTicks);
		ui.$fps.bind(gameLoop.$fps);
	}
}