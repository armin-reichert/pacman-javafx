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

package de.amr.games.pacman.ui.fx.app;

import static de.amr.games.pacman.ui.fx.util.Ufx.alt;
import static de.amr.games.pacman.ui.fx.util.Ufx.just;
import static de.amr.games.pacman.ui.fx.util.Ufx.shift;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

/**
 * @author Armin Reichert
 */
public class PacManGames2d {

	//@formatter:off
	public static final BooleanProperty    PY_SHOW_DEBUG_INFO    = new SimpleBooleanProperty(false);
	public static final IntegerProperty    PY_SIMULATION_STEPS   = new SimpleIntegerProperty(1);

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
	public static final KeyCodeCombination KEY_PLAY_CUTSCENES    = alt(KeyCode.Z);

	public static final KeyCodeCombination KEY_SHOW_HELP         = just(KeyCode.H);
	public static final KeyCodeCombination KEY_BOOT              = just(KeyCode.F3);
	public static final KeyCodeCombination KEY_FULLSCREEN        = just(KeyCode.F11);

	public static PacManGames2dUI     ui;
	//@formatter:on
}
