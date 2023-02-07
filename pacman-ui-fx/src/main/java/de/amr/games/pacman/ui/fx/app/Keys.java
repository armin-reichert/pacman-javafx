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

	public static final KeyCodeCombination CHEAT_EAT_ALL = new KeyCodeCombination(KeyCode.E, KeyCombination.ALT_DOWN);
	public static final KeyCodeCombination CHEAT_ADD_LIVES = new KeyCodeCombination(KeyCode.L, KeyCombination.ALT_DOWN);
	public static final KeyCodeCombination CHEAT_NEXT_LEVEL = new KeyCodeCombination(KeyCode.N, KeyCombination.ALT_DOWN);
	public static final KeyCodeCombination CHEAT_KILL_GHOSTS = new KeyCodeCombination(KeyCode.X, KeyCombination.ALT_DOWN);

	public static final KeyCode AUTOPILOT = KeyCode.A;
	public static final KeyCode DEBUG_INFO = KeyCode.D;
	public static final KeyCode IMMUNITIY = KeyCode.I;
	public static final KeyCode MUTE = KeyCode.M;
	public static final KeyCode PAUSE = KeyCode.P;
	public static final KeyCode QUIT = KeyCode.Q;
	public static final KeyCode TEST_LEVELS = KeyCode.T;
	public static final KeyCode SELECT_VARIANT = KeyCode.V;
	public static final KeyCode PLAY_CUTSCENES = KeyCode.Z;
	public static final KeyCode SINGLE_STEP = KeyCode.SPACE;

	public static final KeyCode START_GAME = KeyCode.DIGIT1;
	public static final KeyCode USE_3D = KeyCode.DIGIT3;
	public static final KeyCode ADD_CREDIT = KeyCode.DIGIT5;

	public static final KeyCode DASHBOARD = KeyCode.F1;
	public static final KeyCode PIP_VIEW = KeyCode.F2;
	public static final KeyCode BOOT = KeyCode.F3;
	public static final KeyCode FULLSCREEN = KeyCode.F11;

	public static final KeyCode PAC_UP = KeyCode.UP;
	public static final KeyCode PAC_DOWN = KeyCode.DOWN;
	public static final KeyCode PAC_LEFT = KeyCode.LEFT;
	public static final KeyCode PAC_RIGHT = KeyCode.RIGHT;

	public static final KeyCode PREV_CAMERA = KeyCode.LEFT;
	public static final KeyCode NEXT_CAMERA = KeyCode.RIGHT;
}