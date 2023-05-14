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

package de.amr.games.pacman.ui.fx.v3d.app;

import static de.amr.games.pacman.ui.fx.util.Ufx.alt;
import static de.amr.games.pacman.ui.fx.util.Ufx.just;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

public class Game3dKeys {
	public static final KeyCodeCombination TOGGLE_DASHBOARD_VISIBLE = just(KeyCode.F1);
	public static final KeyCodeCombination TOGGLE_DASHBOARD_VISIBLE_2 = alt(KeyCode.B);
	public static final KeyCodeCombination TOGGLE_PIP_VIEW_VISIBLE = just(KeyCode.F2);
	public static final KeyCodeCombination TOGGLE_3D_ENABLED = alt(KeyCode.DIGIT3);
	public static final KeyCodeCombination PREV_CAMERA = alt(KeyCode.LEFT);
	public static final KeyCodeCombination NEXT_CAMERA = alt(KeyCode.RIGHT);
}