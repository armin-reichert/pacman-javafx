/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.model.GameModel;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.ResourceBundle;

import static de.amr.games.pacman.ui.fx.input.Keyboard.*;

/**
 * @author Armin Reichert
 */
public class PacManGames2dApp extends Application {
	public static final ResourceBundle TEXTS = ResourceBundle.getBundle(
			"de.amr.games.pacman.ui.fx.texts.messages",
			PacManGames2dApp.class.getModule());

	public static final int CANVAS_WIDTH_UNSCALED  = GameModel.TILES_X * Globals.TS; // 224
	public static final int CANVAS_HEIGHT_UNSCALED = GameModel.TILES_Y * Globals.TS; // 288

	public static final BooleanProperty PY_SHOW_DEBUG_INFO    = new SimpleBooleanProperty(false);

	public static final KeyCodeCombination KEY_CHEAT_EAT_ALL     = alt(KeyCode.E);
	public static final KeyCodeCombination KEY_CHEAT_ADD_LIVES   = alt(KeyCode.L);
	public static final KeyCodeCombination KEY_CHEAT_NEXT_LEVEL  = alt(KeyCode.N);
	public static final KeyCodeCombination KEY_CHEAT_KILL_GHOSTS = alt(KeyCode.X);

	public static final KeyCodeCombination KEY_AUTOPILOT         = alt(KeyCode.A);
	public static final KeyCodeCombination KEY_DEBUG_INFO        = alt(KeyCode.D);
	public static final KeyCodeCombination KEY_IMMUNITY          = alt(KeyCode.I);

	public static final KeyCodeCombination KEY_PAUSE             = just(KeyCode.P);
	public static final KeyCodeCombination[] KEYS_SINGLE_STEP    = { just(KeyCode.SPACE), shift(KeyCode.P) };
	public static final KeyCodeCombination KEY_TEN_STEPS         = shift(KeyCode.SPACE);
	public static final KeyCodeCombination KEY_SIMULATION_FASTER = alt(KeyCode.PLUS);
	public static final KeyCodeCombination KEY_SIMULATION_SLOWER = alt(KeyCode.MINUS);
	public static final KeyCodeCombination KEY_SIMULATION_NORMAL = alt(KeyCode.DIGIT0);

	public static final KeyCodeCombination[] KEYS_START_GAME     = { just(KeyCode.DIGIT1), just(KeyCode.NUMPAD1) };
	public static final KeyCodeCombination[] KEYS_ADD_CREDIT     = { just(KeyCode.DIGIT5), just(KeyCode.NUMPAD5) };

	public static final KeyCodeCombination KEY_QUIT              = just(KeyCode.Q);
	public static final KeyCodeCombination KEY_TEST_LEVELS       = alt(KeyCode.T);
	public static final KeyCodeCombination KEY_SELECT_VARIANT    = just(KeyCode.V);
	public static final KeyCodeCombination KEY_PLAY_CUTSCENES    = alt(KeyCode.C);

	public static final KeyCodeCombination KEY_SHOW_HELP         = just(KeyCode.H);
	public static final KeyCodeCombination KEY_BOOT              = just(KeyCode.F3);
	public static final KeyCodeCombination KEY_FULLSCREEN        = just(KeyCode.F11);

	private final Settings settings = new Settings();
	private PacManGames2dUI ui;

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
		ui = new PacManGames2dUI(stage, settings);
		GameController.it().addListener(ui);
		ui.showStartPage();
		Logger.info("UI initialized. Stage size: {0} x {0} px", stage.getWidth(), stage.getHeight());
	}

	@Override
	public void stop() {
		ui.gameClock().stop();
		Logger.info("Game stopped.");
	}

}