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

import static de.amr.games.pacman.lib.Option.booleanOption;
import static de.amr.games.pacman.lib.Option.doubleOption;
import static de.amr.games.pacman.lib.Option.option;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.lib.Option;
import de.amr.games.pacman.lib.OptionParser;
import de.amr.games.pacman.lib.U;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.model.pacman.PacManGame;
import de.amr.games.pacman.ui.fx._3d.scene.cams.Perspective;
import de.amr.games.pacman.ui.fx.shell.GameUI;
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
		launch(PacManGameAppFX.class, args);
	}

	private static final Logger logger = LogManager.getFormatterLogger();

	//@formatter:off
	static Option<Boolean>     opt3D = booleanOption("-3D", false);
	static Option<Boolean>     optFullscreen = booleanOption("-fullscreen", false);
	static Option<Boolean>     optMuted = booleanOption("-muted", false);
	static Option<Perspective> optPerspective = option("-psp", Perspective.NEAR_PLAYER, Perspective::valueOf);
	static Option<GameVariant> optVariant = option("-variant", GameVariant.PACMAN, GameVariant::valueOf);
	static Option<Double>      optZoom = doubleOption("-zoom", 2.0);
	//@formatter:on

	private final GameController gameController = new GameController();

	@Override
	public void init() throws Exception {
		logger.info("Initializing application...");
		var parser = new OptionParser(opt3D, optFullscreen, optMuted, optPerspective, optVariant, optZoom);
		parser.parse(getParameters().getUnnamed());
		Env.use3D.set(opt3D.getValue());
		Env.perspective.set(optPerspective.getValue());
		gameController.selectGame(optVariant.getValue());
		logger.info("Application initialized. Game variant: %s", gameController.game().variant);
	}

	@Override
	public void start(Stage stage) throws IOException {
		logger.info("Starting application...");
		var zoom = optZoom.getValue();
		var fullscreen = optFullscreen.getValue();
		stage.setFullScreen(fullscreen);
		var ui = new GameUI(gameController, stage, zoom * ArcadeWorld.MODELSIZE.x, zoom * ArcadeWorld.MODELSIZE.y);
		logger.info(() -> "UI size: %.0f x %.0f, zoom: %.2f, 3D: %s, perspective: %s".formatted(stage.getWidth(),
				stage.getHeight(), zoom, U.onOff(Env.use3D.get()), Env.perspective.get()));
		ui.startGameLoop();
	}
}