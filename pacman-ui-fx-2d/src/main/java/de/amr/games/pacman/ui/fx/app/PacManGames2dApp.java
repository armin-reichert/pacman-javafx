/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.app;

import java.util.Locale;

import org.tinylog.Logger;

import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * @author Armin Reichert
 */
public class PacManGames2dApp extends Application {

	protected final Settings settings = new Settings();

	@Override
	public void init() {
		if (getParameters() != null) {
			settings.merge(getParameters().getNamed());
		}
		Logger.info("Game initialized: {}", settings);
	}

	@Override
	public void start(Stage stage) {
		PacManGames2d.ui = new PacManGames2dUI();
		PacManGames2d.ui.init(stage, settings, new ArcadeTheme(PacManGames2d.MGR));
		Logger.info("Game started. {} Hz language={}", PacManGames2d.ui.clock().targetFrameratePy.get(),
				Locale.getDefault());
	}

	@Override
	public void stop() {
		PacManGames2d.ui.clock().stop();
		Logger.info("Game stopped.");
	}
}