package de.amr.games.pacman.ui.fx.app;

import java.io.IOException;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.GameType;
import de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * The Pac-Man game app running in a JavaFX UI.
 * 
 * @author Armin Reichert
 */
public class PacManGameAppFX extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws IOException {
		PacManGameController controller = new PacManGameController();
		controller.play(GameType.PACMAN);
		controller.addView(new PacManGameUI_JavaFX(stage, controller, 2.0));
		controller.showViews();
		controller.startGameLoop();
	}
}