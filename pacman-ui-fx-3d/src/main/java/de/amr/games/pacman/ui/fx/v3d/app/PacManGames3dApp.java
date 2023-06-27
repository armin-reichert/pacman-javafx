/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.app;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.ui.fx.app.Settings;
import de.amr.games.pacman.ui.fx.scene.GameSceneConfiguration;
import de.amr.games.pacman.ui.fx.scene2d.*;
import de.amr.games.pacman.ui.fx.v3d.scene.PlayScene3D;
import javafx.application.Application;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.Locale;

/**
 * @author Armin Reichert
 */
public class PacManGames3dApp extends Application {

	private final PacManGames3dUI ui = new PacManGames3dUI();
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
				new PlayScene3D(),
				new MsPacManCutscene1(),
				new MsPacManCutscene2(),
				new MsPacManCutscene3()
		);
		var gameScenesPacMan = new GameSceneConfiguration(
				new BootScene(),
				new PacManIntroScene(),
				new PacManCreditScene(),
				new PlayScene2D(),
				new PlayScene3D(),
				new PacManCutscene1(),
				new PacManCutscene2(),
				new PacManCutscene3()
		);
		ui.init(stage, settings, gameScenesMsPacMan, gameScenesPacMan);
		ui.setTheme(PacManGames3d.createTheme());
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