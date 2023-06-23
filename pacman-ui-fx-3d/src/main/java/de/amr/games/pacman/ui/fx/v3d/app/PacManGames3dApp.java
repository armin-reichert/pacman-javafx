/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.app;

import de.amr.games.pacman.ui.fx.app.PacManGames2d;
import de.amr.games.pacman.ui.fx.app.PacManGames2dApp;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.Locale;

/**
 * @author Armin Reichert
 */
public class PacManGames3dApp extends PacManGames2dApp {

	private PacManGames3dUI ui;

	@Override
	public void init() {
		settings.merge(getParameters().getNamed());
		Logger.info("Game initialized: {}", settings);
	}

	@Override
	public void start(Stage stage) {
		var theme = new ArcadeTheme(PacManGames2d.MGR);
		// TODO
		PacManGames3d.addThings(theme);

		ui = new PacManGames3dUI();
		ui.init(stage, settings, theme);
		Logger.info("Game started. {} Hz language={}", ui.clock().targetFrameratePy.get(), Locale.getDefault());
	}

	@Override
	public void stop() {
		ui.clock().stop();
		Logger.info("Game stopped.");
	}
}