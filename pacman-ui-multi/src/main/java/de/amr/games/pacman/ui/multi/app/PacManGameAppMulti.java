package de.amr.games.pacman.ui.multi.app;

import javax.swing.SwingUtilities;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX;
import de.amr.games.pacman.ui.swing.PacManGameUI_Swing;
import javafx.animation.AnimationTimer;
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
		try {
			SwingUtilities.invokeLater(() -> {
				controller.addView(new PacManGameUI_Swing(controller, 576));
			});
			Thread.sleep(100);
		} catch (InterruptedException x) {
			x.printStackTrace();
		}
		controller.addView(new PacManGameUI_JavaFX(stage, controller, 576));
		controller.showViews();
		new AnimationTimer() {

			@Override
			public void handle(long now) {
				controller.step();
			}
		}.start();
	}
}