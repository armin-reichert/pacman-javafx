/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.model.pacman.PacManGame;
import de.amr.games.pacman.ui.fx.shell.Actions;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * This is the entry point of the Pac-Man and Ms. Pac-Man games.
 * 
 * <p>
 * The application is structured according to the MVC (model-view-controller) design pattern.
 * 
 * <p>
 * The model layer consists of the two game models {@link PacManGame} and {@link MsPacManGame}. The controller
 * {@link GameController} is a finite-state machine which is triggered 60 times per second by the {@link GameLoop}. The
 * view {@link GameUI} listens to game events which are sent by the controller.
 * <p>
 * The model and controller layers are decoupled from the view layer which allow to create different user interfaces for
 * the games without any change in the controller or model. As a proof of concept there exists also a Swing user
 * interface.
 * 
 * @author Armin Reichert
 */
public class PacManGameAppFX extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	private Options options;
	private GameController gameController;

	@Override
	public void init() throws Exception {
		log("Initializing application...");
		options = new Options(getParameters().getUnnamed());
		Env.$3D.set(options.use3D);
		Env.$perspective.set(options.perspective);
		gameController = new GameController();
		gameController.selectGame(options.gameVariant);
		log("Application initialized. Game variant: %s", gameController.game().variant);
	}

	@Override
	public void start(Stage stage) throws IOException {
		log("Starting application...");
		double zoom = options.zoom;
		var ui = new GameUI(gameController, stage, zoom * ArcadeWorld.SIZE.x, zoom * ArcadeWorld.SIZE.y);
		ui.setFullScreen(options.fullscreen);
		Actions.init(gameController, ui);
		GameLoop.get().update = ui::update;
		GameLoop.get().render = ui::render;
		GameLoop.get().setTargetFrameRate(60);
		GameLoop.get().setTimeMeasured(false);
		GameLoop.get().start();
		log("Application started.");
		log("UI size: %.0f x %.0f, zoom: %.2f, 3D: %s, perspective: %s", ui.getWidth(), ui.getHeight(), zoom,
				U.onOff(Env.$3D.get()), Env.$perspective.get());
	}
}