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

import java.io.IOException;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.fx.shell.PacManGameUI_JavaFX;
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
	private final GameLoop gameLoop = new GameLoop();

	public static void main(String[] args) {
		options = new Options(args);
		launch(args);
	}

	@Override
	public void init() throws Exception {
		gameController = new PacManGameController();
		gameController.selectGame(options.gameVariant);
	}

	@Override
	public void start(Stage stage) throws IOException {
		PacManGameUI_JavaFX ui = new PacManGameUI_JavaFX(stage, gameController, options.height);
		PacManGameUI_JavaFX.$TOTAL_TICKS.bind(gameLoop.$totalTicks);
		PacManGameUI_JavaFX.$FPS.bind(gameLoop.$fps);
		gameController.setUI(ui);
		gameLoop.addTask("Controller Step", gameController::step);
		gameLoop.addTask("UI Update", ui::update);
		gameLoop.start();
	}
}