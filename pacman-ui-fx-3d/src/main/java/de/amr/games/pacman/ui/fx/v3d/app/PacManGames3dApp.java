/*
MIT License

Copyright (c) 2023 Armin Reichert

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

import static de.amr.games.pacman.ui.fx.util.ResourceManager.fmtMessage;

import java.util.Locale;

import org.tinylog.Logger;

import de.amr.games.pacman.ui.fx.app.PacManGames2d;
import de.amr.games.pacman.ui.fx.app.PacManGames2dApp;
import de.amr.games.pacman.ui.fx.app.PacManGames2dAssets;
import de.amr.games.pacman.ui.fx.app.Settings;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.application.Application;
import javafx.scene.shape.DrawMode;
import javafx.stage.Stage;

/**
 * @author Armin Reichert
 */
public class PacManGames3dApp extends Application {

	private PacManGames3dUI ui;
	private static Settings cfg;

	@Override
	public void init() {
		cfg = new Settings(getParameters().getNamed());
		PacManGames2d.app = new PacManGames2dApp();
		PacManGames2d.assets = new PacManGames2dAssets();
		PacManGames3d.app = this;
		PacManGames3d.assets = new PacManGames3dAssets();
		Logger.info("Game initialized, configuration: {}", cfg);
	}

	@Override
	public void start(Stage stage) {
		stage.setFullScreen(cfg.fullScreen);
		ui = new PacManGames3dUI(cfg.variant, stage, cfg.zoom * 28 * 8, cfg.zoom * 36 * 8);
		PacManGames3d.ui = ui;
		ui.init(cfg);
		ui.startClockAndShowStage();
		PacManGames2d.app.reboot();
		Logger.info("Game started. {} Hz language={}", ui.clock().targetFrameratePy.get(), Locale.getDefault());
	}

	@Override
	public void stop() {
		ui.clock().stop();
		Logger.info("Game stopped.");
	}

	// --- Actions ---

	public void togglePipVisibility() {
		Ufx.toggle(ui.pip().visiblePy);
		var message = fmtMessage(PacManGames3d.assets.messages, ui.pip().visiblePy.get() ? "pip_on" : "pip_off");
		ui.showFlashMessage(message);
	}

	public void toggleDashboardVisible() {
		ui.dashboard().setVisible(!ui.dashboard().isVisible());
	}

	public void selectNextPerspective() {
		var next = PacManGames3d.PY_3D_PERSPECTIVE.get().next();
		PacManGames3d.PY_3D_PERSPECTIVE.set(next);
		String perspectiveName = fmtMessage(PacManGames3d.assets.messages, next.name());
		ui.showFlashMessage(fmtMessage(PacManGames3d.assets.messages, "camera_perspective", perspectiveName));
	}

	public void selectPrevPerspective() {
		var prev = PacManGames3d.PY_3D_PERSPECTIVE.get().prev();
		PacManGames3d.PY_3D_PERSPECTIVE.set(prev);
		String perspectiveName = fmtMessage(PacManGames3d.assets.messages, prev.name());
		ui.showFlashMessage(fmtMessage(PacManGames3d.assets.messages, "camera_perspective", perspectiveName));
	}

	public void toggleDrawMode() {
		PacManGames3d.PY_3D_DRAW_MODE
				.set(PacManGames3d.PY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
	}
}