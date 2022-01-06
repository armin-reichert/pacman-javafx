/*
MIT License

Copyright (c) 2021 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx.app;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.GameVariant.PACMAN;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx._3d.scene.Perspective;
import de.amr.games.pacman.ui.fx.shell.PacManGameUI_JavaFX;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.stage.Stage;

/**
 * The Pac-Man / Ms. Pac-Man game running in a JavaFX UI.
 * 
 * @author Armin Reichert
 */
public class PacManGameAppFX extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws IOException {
		Options options = new Options(getParameters().getUnnamed());
		PacManGameController gameController = new PacManGameController(options.gameVariant);
		PacManGameUI_JavaFX ui = new PacManGameUI_JavaFX(stage, gameController, options.windowHeight);
		gameController.setUI(ui);
		gameController.setPlayerControl(ui);
		Env.gameLoop = new GameLoop(() -> {
			gameController.updateState();
			ui.getCurrentGameScene().update();
		}, ui::update);
		Env.$use3DScenes.set(options.use3DScenes);
		Env.$perspective.set(options.perspective);
		stage.titleProperty().bind(Bindings.createStringBinding(() -> {
			String gameName = gameController.gameVariant() == PACMAN ? "Pac-Man" : "Ms. Pac-Man";
			return Env.$paused.get() ? String.format("%s (JavaFX, Game PAUSED)", gameName)
					: String.format("%s (JavaFX)", gameName);
		}, Env.gameLoop.$fps));
		log("Application started, game variant: %s, window height: %.0f, 3D: %s, perspective: %s", options.gameVariant,
				options.windowHeight, options.use3DScenes, options.perspective);
		Env.gameLoop.start();
	}

	private static class Options {

		static final String[] NAMES = { "-2D", "-3D", "-height", "-mspacman", "-pacman", "-perspective" };

		boolean use3DScenes = true;
		double windowHeight = 576;
		GameVariant gameVariant = GameVariant.PACMAN;
		Perspective perspective = Perspective.CAM_FOLLOWING_PLAYER;

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

				else if ("-perspective".equals(params.get(i))) {
					if (i + 1 == params.size() || parameterNamesList.contains(params.get(i + 1))) {
						log("!!! Error parsing parameters: missing perspective value.");
					} else {
						++i;
						try {
							perspective = Perspective.valueOf(params.get(i).toUpperCase());
						} catch (Exception x) {
							log("!!! Error parsing parameters: '%s' is no legal perspective value.", params.get(i));
						}
					}
				}

				else {
					log("!!! Error parsing parameters: Found garbage '%s'", params.get(i));
				}

				++i;
			}
		}
	}
}