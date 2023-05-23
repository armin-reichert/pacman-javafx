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
 * @author Armin Reichert
 */
public class PacManGames2dApp extends Application {

	protected final Settings settings = new Settings();

	@Override
	public void init() {
		settings.merge(getParameters().getNamed());
		Logger.info("Game initialized: {}", settings);
	}

	@Override
	public void start(Stage stage) {
		var assets = new PacManGames2dAssets();
		PacManGames2d.ui = new PacManGames2dUI();
		PacManGames2d.ui.init(stage, settings, assets, assets.arcadeTheme);
		PacManGames2d.ui.gameController().changeState(GameState.BOOT);
		PacManGames2d.ui.show();
		Logger.info("Game started. {} Hz language={}", PacManGames2d.ui.clock().targetFrameratePy.get(),
				Locale.getDefault());
	}

	@Override
	public void stop() {
		PacManGames2d.ui.clock().stop();
		Logger.info("Game stopped.");
	}
}