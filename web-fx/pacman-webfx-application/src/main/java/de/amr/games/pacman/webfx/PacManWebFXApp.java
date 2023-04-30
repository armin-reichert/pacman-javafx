package de.amr.games.pacman.webfx;

import java.io.IOException;
import java.util.Collections;

import org.tinylog.Logger;

import de.amr.games.pacman.ui.fx.app.AppRes;
import de.amr.games.pacman.ui.fx.app.Settings;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import javafx.application.Application;
import javafx.stage.Stage;

public class PacManWebFXApp extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	private GameUI gameUI;

	@Override
	public void init() throws Exception {
		AppRes.load();
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		gameUI = new GameUI(primaryStage, new Settings(Collections.emptyMap()));
		gameUI.simulation().start();
		Logger.info("Game started. Target frame rate: {}", gameUI.simulation().targetFrameratePy.get());
	}

	@Override
	public void stop() throws Exception {
		gameUI.simulation().stop();
		Logger.info("Game stopped");
	}

}