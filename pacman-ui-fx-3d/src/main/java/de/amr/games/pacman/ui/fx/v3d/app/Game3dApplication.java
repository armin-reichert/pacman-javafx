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
package de.amr.games.pacman.ui.fx.v3d.app;

import java.io.IOException;
import java.util.Locale;

import org.tinylog.Logger;

import de.amr.games.pacman.ui.fx.app.Game2d;
import de.amr.games.pacman.ui.fx.app.Game2dActions;
import de.amr.games.pacman.ui.fx.app.Game2dAssets;
import de.amr.games.pacman.ui.fx.app.Settings;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * @author Armin Reichert
 */
public class Game3dApplication extends Application {

	@Override
	public void init() throws Exception {
		long start = System.nanoTime();
		Game2d.assets = new Game2dAssets();
		var assets = new Game3dAssets();
		Logger.info("Loading assets: {} seconds.", (System.nanoTime() - start) / 1e9f);
		Game2d.actions = new Game2dActions();

		// make everything statically accessible
		Game3d.actions = new Game3dActions();
		Game3d.app = this;
		Game3d.assets = assets;
	}

	@Override
	public void start(Stage stage) throws IOException {
		var cfg = new Settings(getParameters().getNamed());

		Game3d.ui = new Game3dUI(cfg.variant, stage, cfg.zoom * 28 * 8, cfg.zoom * 36 * 8);
		Game3d.ui.init(cfg);

		Game2d.actions.setUI(Game3d.ui);

		Game2d.actions.reboot();
		stage.setFullScreen(cfg.fullScreen);
		Game3d.ui.startClockAndShowStage();

		Logger.info("Game started. {} Hz language={} {}", Game3d.ui.clock().targetFrameratePy.get(), Locale.getDefault(),
				cfg);
	}

	@Override
	public void stop() throws Exception {
		Game3d.ui.clock().stop();
		Logger.info("Game stopped.");
	}
}