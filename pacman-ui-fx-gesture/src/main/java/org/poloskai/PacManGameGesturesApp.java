package org.poloskai;

import static de.amr.games.pacman.lib.Logging.log;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.Logging;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx.app.GameLoop;
import de.amr.games.pacman.ui.fx.shell.PacManGameUI_JavaFX;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class PacManGameGesturesApp extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws IOException {

		// create gesture producer and consumer
		GestureProducer gp = new GestureProducer();
		GestureConsumer gc = new GestureConsumer(gp);

		Options options = new Options(getParameters().getUnnamed());
		log("Game variant: %s, window height: %.0f, 3D: %s", options.gameVariant, options.windowHeight,
				options.use3DScenes);
		PacManGameController gameController = new PacManGameController();
		gameController.selectGameVariant(options.gameVariant);
		PacManGameUI_JavaFX ui = new PacManGameUI_JavaFX(stage, gameController, options.windowHeight);
		gameController.setUI(ui);

		// use gesture consumer to control player
		gameController.setPlayerControl(gc);

		Env.gameLoop = new GameLoop(() -> {
			gameController.updateState();
			ui.getCurrentGameScene().update();
		}, ui::update);
		Env.$totalTicks.bind(Env.gameLoop.$totalTicks);
		Env.$fps.bind(Env.gameLoop.$fps);
		Env.$use3DScenes.set(options.use3DScenes);

		// start producing gestures
		gp.start();

		// stop producing gestures when game is exited / window closed
		ui.getStage().addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, e -> {
			gp.stop();
			Logging.log("Gesture producer stopped");
		});

		Env.gameLoop.start();
	}

	private static class Options {

		static final String[] NAMES = { "-2D", "-3D", "-height", "-mspacman", "-pacman" };

		double windowHeight = 576;
		boolean use3DScenes = true;
		GameVariant gameVariant = GameVariant.PACMAN;

		Options(List<String> params) {
			List<String> parameterNamesList = Arrays.asList(NAMES);
			int i = 0;
			while (i < params.size()) {

				if ("-height".equals(params.get(i))) {
					if (i + 1 == params.size() || parameterNamesList.contains(params.get(i + 1))) {
						log("!!! Error parsing parameters: missing height value.");
					} else {
						++i;
						try {
							windowHeight = Double.parseDouble(params.get(i));
						} catch (NumberFormatException x) {
							log("!!! Error parsing parameters: '%s' is no legal height value.", params.get(i));
						}
					}
				}

				else if ("-mspacman".equals(params.get(i))) {
					gameVariant = GameVariant.MS_PACMAN;
				}

				else if ("-pacman".equals(params.get(i))) {
					gameVariant = GameVariant.PACMAN;
				}

				else if ("-2D".equals(params.get(i))) {
					use3DScenes = false;
				}

				else if ("-3D".equals(params.get(i))) {
					use3DScenes = true;
				}

				else {
					log("!!! Error parsing parameters: Found garbage '%s'", params.get(i));
				}

				++i;
			}
		}
	}
}