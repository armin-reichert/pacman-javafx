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

import java.io.IOException;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx.shell.ManualPlayerControl;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * This is the entry point of the Pac-Man and Ms. Pac-Man games.
 * 
 * <p>
 * The application structure follows the MVC (model-view-controller) design pattern. It creates the controller (which in
 * turn creates the model(s)) and the view (JavaFX UI). A game loop drives the controller which implements the complete
 * game logic. Game events from the controller are handled by the UI.
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

		// Create the game controller and the game models, select the specified game variant
		var controller = new GameController(options.gameVariant);

		// By default, player is controlled using keyboard
		var playerControl = new ManualPlayerControl(stage);

		// Create the user interface and the connections with the controllers
		var ui = new GameUI(stage, controller, options.windowHeight, options.fullscreen);
		controller.addGameEventListener(ui);
		controller.setPlayerControl(playerControl);

		// Initialize the environment and start the game
		Env.$3D.set(options.use3DScenes);
		Env.$perspective.set(options.perspective);
		Env.sounds = options.gameVariant == GameVariant.MS_PACMAN ? GameUI.SOUNDS_MSPACMAN
				: GameUI.SOUNDS_PACMAN;
		Env.gameLoop.update = () -> {
			controller.updateState();
			ui.updateGameScene();
		};
		Env.gameLoop.render = ui::update;
		Env.gameLoop.start();

		log("Application started. Game variant: %s, window height: %.0f, 3D: %s, camera perspective: %s",
				options.gameVariant, options.windowHeight, options.use3DScenes, options.perspective);
	}
}