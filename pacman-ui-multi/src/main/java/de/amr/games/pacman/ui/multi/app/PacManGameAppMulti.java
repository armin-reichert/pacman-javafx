package de.amr.games.pacman.ui.multi.app;

import javax.swing.SwingUtilities;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX;
import de.amr.games.pacman.ui.swing.PacManGameUI_Swing;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * The Pac-Man game app.
 * 
 * @author Armin Reichert
 */
public class PacManGameAppMulti extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		PacManGameController controller = new PacManGameController();
		controller.play(GameType.PACMAN);
		SwingUtilities.invokeLater(() -> {
			controller.addView(new PacManGameUI_Swing(controller, 2.0));
		});
		controller.addView(new PacManGameUI_JavaFX(stage, controller, 2.0));
		controller.showViews();
		controller.startGameLoop();
	}
}