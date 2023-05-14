/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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
import java.util.Locale;

import org.tinylog.Logger;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * This is 2D-only version of the Pac-Man and Ms. Pac-Man games.
 * 
 * <p>
 * The application is structured according to the MVC (model-view-controller) design pattern. The model layer consists
 * of the two game models <code> PacManGame</code> and <code> MsPacManGame</code>. The controller is a finite-state
 * machine which is triggered 60 times per second by the game loop. The user interface listens to game events sent from
 * the controller/model layer. The model and controller layers are decoupled from the user interface. This allow to
 * attach different user interfaces without having to change the controller or model.
 * 
 * <p>
 * As a proof of concept I implemented also a (simpler) Swing user interface, see repository
 * <a href="https://github.com/armin-reichert/pacman-ui-swing">Pac-Man Swing UI</a>.
 * 
 * @author Armin Reichert
 */
public class Game2dApplication extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void init() throws Exception {
		Game2d.actions = new Game2dActions();
		Game2d.app = this;
		Game2d.assets = new Game2dAssets();
		Game2d.assets.load();
	}

	@Override
	public void start(Stage stage) throws IOException {
		var cfg = new Settings(getParameters().getNamed());
		Game2d.ui = new Game2dUI(cfg.variant, stage, cfg.zoom * 28 * 8, cfg.zoom * 36 * 8);
		Game2d.ui.init(cfg);
		Game2d.actions.setUI(Game2d.ui);
		Game2d.actions.reboot();
		stage.setFullScreen(cfg.fullScreen);
		Game2d.ui.startClockAndShowStage();
		Logger.info("Game started. {} Hz language={} {}", Game2d.ui.clock().targetFrameratePy.get(), Locale.getDefault(),
				cfg);
	}

	@Override
	public void stop() throws Exception {
		Game2d.ui.clock().stop();
		Logger.info("Game stopped.");
	}
}