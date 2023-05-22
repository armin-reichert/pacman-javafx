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

import java.util.Locale;

import org.tinylog.Logger;

import de.amr.games.pacman.controller.GameState;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * This is 2D version of the Pac-Man and Ms. Pac-Man games.
 * 
 * @author Armin Reichert
 */
public class PacManGames2dApp extends Application {

	private final Settings cfg = new Settings();

	@Override
	public void init() {
		cfg.merge(getParameters().getNamed());
		PacManGames2d.assets = new PacManGames2dAssets();
		Logger.info("Game initialized, configuration: {}", cfg);
	}

	@Override
	public void start(Stage stage) {
		stage.setFullScreen(cfg.fullScreen);
		PacManGames2d.ui = new PacManGames2dUI(cfg.variant, stage, cfg.zoom * 28 * 8, cfg.zoom * 36 * 8);
		PacManGames2d.ui.init(cfg);
		PacManGames2d.ui.gameController().changeState(GameState.BOOT);
		PacManGames2d.ui.showStage();
		PacManGames2d.ui.clock.start();
		Logger.info("Game started. {} Hz language={}", PacManGames2d.ui.clock().targetFrameratePy.get(),
				Locale.getDefault());
	}

	@Override
	public void stop() {
		PacManGames2d.ui.clock().stop();
		Logger.info("Game stopped.");
	}
}