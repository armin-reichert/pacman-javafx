/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.app;

import java.util.Locale;

import org.tinylog.Logger;

import de.amr.games.pacman.ui.fx.app.PacManGames2d;
import de.amr.games.pacman.ui.fx.app.PacManGames2dApp;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import javafx.stage.Stage;

/**
 * @author Armin Reichert
 */
public class PacManGames3dApp extends PacManGames2dApp {

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

		PacManGames2d.ui = PacManGames3d.ui = new PacManGames3dUI();
		PacManGames3d.ui.init(stage, settings, theme);
		Logger.info("Game started. {} Hz language={}", PacManGames3d.ui.clock().targetFrameratePy.get(),
				Locale.getDefault());
	}

	@Override
	public void stop() {
		PacManGames3d.ui.clock().stop();
		Logger.info("Game stopped.");
	}
}