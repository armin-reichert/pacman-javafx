/*
MIT License

Copyright (c) 2022 Armin Reichert

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

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * @author Armin Reichert
 */
public class Keys {

	private static KeyCodeCombination just(KeyCode code) {
		return new KeyCodeCombination(code);
	}

	private static KeyCodeCombination alt(KeyCode code) {
		return new KeyCodeCombination(code, KeyCombination.ALT_DOWN);
	}

	private static KeyCodeCombination shift(KeyCode code) {
		return new KeyCodeCombination(code, KeyCombination.SHIFT_DOWN);
	}

	public static final KeyCodeCombination CHEAT_EAT_ALL = alt(KeyCode.E);
	public static final KeyCodeCombination CHEAT_ADD_LIVES = alt(KeyCode.L);
	public static final KeyCodeCombination CHEAT_NEXT_LEVEL = alt(KeyCode.N);
	public static final KeyCodeCombination CHEAT_KILL_GHOSTS = alt(KeyCode.X);

	public static final KeyCodeCombination AUTOPILOT = alt(KeyCode.A);
	public static final KeyCodeCombination DEBUG_INFO = alt(KeyCode.D);
	public static final KeyCodeCombination IMMUNITIY = alt(KeyCode.I);
	public static final KeyCodeCombination MUTE = alt(KeyCode.M);

	public static final KeyCodeCombination PAUSE = just(KeyCode.P);
	public static final KeyCodeCombination PAUSE_STEP = shift(KeyCode.P);
	public static final KeyCodeCombination SINGLE_STEP = just(KeyCode.SPACE);
	public static final KeyCodeCombination TEN_STEPS = shift(KeyCode.SPACE);

	public static final KeyCodeCombination QUIT = just(KeyCode.Q);
	public static final KeyCodeCombination TEST_LEVELS = alt(KeyCode.T);
	public static final KeyCodeCombination SELECT_VARIANT = just(KeyCode.V);
	public static final KeyCodeCombination PLAY_CUTSCENES = alt(KeyCode.Z);

	public static final KeyCodeCombination START_GAME = just(KeyCode.DIGIT1);
	public static final KeyCodeCombination USE_3D = alt(KeyCode.DIGIT3);
	public static final KeyCodeCombination ADD_CREDIT = just(KeyCode.DIGIT5);

	public static final KeyCodeCombination DASHBOARD = just(KeyCode.F1);
	public static final KeyCodeCombination DASHBOARD2 = alt(KeyCode.B);
	public static final KeyCodeCombination PIP_VIEW = just(KeyCode.F2);
	public static final KeyCodeCombination BOOT = just(KeyCode.F3);
	public static final KeyCodeCombination FULLSCREEN = just(KeyCode.F11);

	public static final KeyCodeCombination PREV_CAMERA = alt(KeyCode.LEFT);
	public static final KeyCodeCombination NEXT_CAMERA = alt(KeyCode.RIGHT);

}