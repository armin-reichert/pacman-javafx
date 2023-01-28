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

import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.lib.U;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.model.pacman.PacManGame;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx.Env3D;
import de.amr.games.pacman.ui.fx._3d.scene.cams.Perspective;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import de.amr.games.pacman.ui.fx.util.GameLoop;
import javafx.application.Application;
import javafx.scene.input.KeyCode;
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
 * view {@link GameUI} listens to game events sent from the controller/model layer.
 * <p>
 * The model and controller layers are decoupled from the user interface. This allow to attach different user interfaces
 * without having to change the controller or model. As a proof of concept I implemented also a (simpler) Swing user
 * interface, see repository {@code pacman-ui-swing}.
 * 
 * @author Armin Reichert
 */
public class PacManGameAppFX extends Application {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	public static void main(String[] args) {
		launch(args);
	}

	private static class AppSettings {
		public boolean fullScreen;
		public final Perspective perspective;
		public final boolean use3D;
		public final GameVariant variant;
		public final float zoom;

		private AppSettings(Map<String, String> namedParams) {
			LOGGER.info("Create application settings from named parameters: %s", namedParams);
			fullScreen = Boolean.valueOf(namedParams.getOrDefault("fullScreen", "false"));
			perspective = Perspective.valueOf(namedParams.getOrDefault("perspective", "NEAR_PLAYER"));
			use3D = Boolean.valueOf(namedParams.getOrDefault("use3D", "false"));
			variant = GameVariant.valueOf(namedParams.getOrDefault("variant", "PACMAN"));
			zoom = Double.valueOf(namedParams.getOrDefault("zoom", "2.0")).floatValue();
		}
	}

	private AppSettings settings;
	private GameController gameController;

	@Override
	public void init() throws Exception {
		settings = new AppSettings(getParameters().getNamed());
		Env.use3DPy.set(settings.use3D);
		Env3D.perspectivePy.set(settings.perspective);
		gameController = new GameController(settings.variant);
		LOGGER.info("Application initialized. Game variant: %s", gameController.game().variant());
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		var perspective = Env3D.perspectivePy.get();
		var ui = new GameUI(gameController, primaryStage, settings.zoom, settings.fullScreen);
		ui.setSteeringKeys(KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT);
		ui.start();
		LOGGER.info("Game started. Target frame rate: %d", ui.gameLoop().getTargetFramerate());
		LOGGER.info(() -> "UI size: %.0f x %.0f, zoom: %.2f, 3D: %s, perspective: %s".formatted(primaryStage.getWidth(),
				primaryStage.getHeight(), settings.zoom, U.onOff(Env.use3DPy.get()), perspective));
	}
}