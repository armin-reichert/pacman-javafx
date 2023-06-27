/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.app;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.scene.GameSceneConfiguration;
import de.amr.games.pacman.ui.fx.scene2d.*;
import javafx.application.Application;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.Locale;

/**
 * @author Armin Reichert
 */
public class PacManGames2dApp extends Application {

	private final PacManGames2dUI ui = new PacManGames2dUI();
	private final Settings settings = new Settings();

	@Override
	public void init() {
		if (getParameters() != null) {
			settings.merge(getParameters().getNamed());
		}
		GameController.create(settings.variant);
		Logger.info("Game initialized: {}", settings);
	}

	@Override
	public void start(Stage stage) {
		var gameScenesMsPacMan = new GameSceneConfiguration(
			new BootScene(),
			new MsPacManIntroScene(),
			new MsPacManCreditScene(),
			new PlayScene2D(),
			null,
			new MsPacManCutscene1(),
			new MsPacManCutscene2(),
			new MsPacManCutscene3()
		);
		var gameScenesPacMan = new GameSceneConfiguration(
			new BootScene(),
			new PacManIntroScene(),
			new PacManCreditScene(),
			new PlayScene2D(),
			null,
			new PacManCutscene1(),
			new PacManCutscene2(),
			new PacManCutscene3()
		);
		ui.init(stage, settings, gameScenesMsPacMan, gameScenesPacMan);
		ui.setTheme(new ArcadeTheme(PacManGames2d.MGR));
		GameController.addListener(ui);
		ui.showStartPage();
		Logger.info("Game started. {} Hz language={}", ui.clock().targetFrameratePy.get(), Locale.getDefault());
	}

	@Override
	public void stop() {
		ui.clock().stop();
		Logger.info("Game stopped.");
	}
}