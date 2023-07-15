/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.app;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.scene.GameSceneConfiguration;
import de.amr.games.pacman.ui.fx.scene2d.*;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Theme;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.Locale;
import java.util.ResourceBundle;

import static de.amr.games.pacman.ui.fx.input.Keyboard.*;
import static de.amr.games.pacman.ui.fx.input.Keyboard.just;

/**
 * @author Armin Reichert
 */
public class PacManGames2dApp extends Application {

	public static final ResourceManager MGR = new ResourceManager("/de/amr/games/pacman/ui/fx/", PacManGames2dApp.class);
	public static final ResourceBundle TEXTS = ResourceBundle.getBundle("de.amr.games.pacman.ui.fx.texts.messages");

	public static final int CANVAS_WIDTH_UNSCALED  = 224;
	public static final int CANVAS_HEIGHT_UNSCALED = 288;

	public static final BooleanProperty PY_SHOW_DEBUG_INFO    = new SimpleBooleanProperty(false);

	public static final KeyCodeCombination KEY_CHEAT_EAT_ALL     = alt(KeyCode.E);
	public static final KeyCodeCombination KEY_CHEAT_ADD_LIVES   = alt(KeyCode.L);
	public static final KeyCodeCombination KEY_CHEAT_NEXT_LEVEL  = alt(KeyCode.N);
	public static final KeyCodeCombination KEY_CHEAT_KILL_GHOSTS = alt(KeyCode.X);

	public static final KeyCodeCombination KEY_AUTOPILOT         = alt(KeyCode.A);
	public static final KeyCodeCombination KEY_DEBUG_INFO        = alt(KeyCode.D);
	public static final KeyCodeCombination KEY_IMMUNITY           = alt(KeyCode.I);

	public static final KeyCodeCombination KEY_PAUSE             = just(KeyCode.P);
	public static final KeyCodeCombination KEY_PAUSE_STEP        = shift(KeyCode.P);
	public static final KeyCodeCombination KEY_SINGLE_STEP       = just(KeyCode.SPACE);
	public static final KeyCodeCombination KEY_TEN_STEPS         = shift(KeyCode.SPACE);
	public static final KeyCodeCombination KEY_SIMULATION_FASTER = alt(KeyCode.PLUS);
	public static final KeyCodeCombination KEY_SIMULATION_SLOWER = alt(KeyCode.MINUS);
	public static final KeyCodeCombination KEY_SIMULATION_NORMAL = alt(KeyCode.DIGIT0);

	public static final KeyCodeCombination KEY_START_GAME        = just(KeyCode.DIGIT1);
	public static final KeyCodeCombination KEY_START_GAME_NUMPAD = just(KeyCode.NUMPAD1);
	public static final KeyCodeCombination KEY_ADD_CREDIT        = just(KeyCode.DIGIT5);
	public static final KeyCodeCombination KEY_ADD_CREDIT_NUMPAD = just(KeyCode.NUMPAD5);

	public static final KeyCodeCombination KEY_QUIT              = just(KeyCode.Q);
	public static final KeyCodeCombination KEY_TEST_LEVELS       = alt(KeyCode.T);
	public static final KeyCodeCombination KEY_SELECT_VARIANT    = just(KeyCode.V);
	public static final KeyCodeCombination KEY_PLAY_CUTSCENES    = alt(KeyCode.C);

	public static final KeyCodeCombination KEY_SHOW_HELP         = just(KeyCode.H);
	public static final KeyCodeCombination KEY_BOOT              = just(KeyCode.F3);
	public static final KeyCodeCombination KEY_FULLSCREEN        = just(KeyCode.F11);


	private final PacManGames2dUI ui = new PacManGames2dUI();
	private Theme theme;
	private final Settings settings = new Settings();

	@Override
	public void init() {
		if (getParameters() != null) {
			settings.merge(getParameters().getNamed());
		}
		theme = new ArcadeTheme(MGR);
		Logger.info("Theme created: {}", theme);
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
		ui.setTheme(theme);
		GameController.addListener(ui);
		ui.showStartPage();
		Logger.info("Game started. {} Hz locale={}", ui.clock().targetFrameratePy.get(), localeInfo());
	}

	@Override
	public void stop() {
		ui.clock().stop();
		Logger.info("Game stopped.");
	}

	private String localeInfo() {
		var language = System.getProperty("user.language");
		var country = System.getProperty("user.country");
		if (language == null) {
			return "no locale info";
		}
		if (country == null) {
			return language;
		}
		return new Locale(language, country).toString();
	}
}