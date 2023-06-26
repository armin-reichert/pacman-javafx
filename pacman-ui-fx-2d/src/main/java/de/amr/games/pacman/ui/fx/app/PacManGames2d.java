/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.app;

import de.amr.games.pacman.ui.fx.util.ResourceManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import java.util.ResourceBundle;

import static de.amr.games.pacman.ui.fx.input.Keyboard.*;

/**
 * @author Armin Reichert
 */
public class PacManGames2d {

	public static final ResourceManager MGR = new ResourceManager("/de/amr/games/pacman/ui/fx/", PacManGames2d.class);
	public static final ResourceBundle TEXTS = ResourceBundle.getBundle("de.amr.games.pacman.ui.fx.texts.messages");

	//@formatter:off
	public static final BooleanProperty    PY_SHOW_DEBUG_INFO    = new SimpleBooleanProperty(false);

	public static final KeyCodeCombination KEY_CHEAT_EAT_ALL     = alt(KeyCode.E);
	public static final KeyCodeCombination KEY_CHEAT_ADD_LIVES   = alt(KeyCode.L);
	public static final KeyCodeCombination KEY_CHEAT_NEXT_LEVEL  = alt(KeyCode.N);
	public static final KeyCodeCombination KEY_CHEAT_KILL_GHOSTS = alt(KeyCode.X);

	public static final KeyCodeCombination KEY_AUTOPILOT         = alt(KeyCode.A);
	public static final KeyCodeCombination KEY_DEBUG_INFO        = alt(KeyCode.D);
	public static final KeyCodeCombination KEY_IMMUNITIY         = alt(KeyCode.I);

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
	//@formatter:on
}