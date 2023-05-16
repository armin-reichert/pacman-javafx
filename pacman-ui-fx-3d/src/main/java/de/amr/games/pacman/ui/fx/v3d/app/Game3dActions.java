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

import static de.amr.games.pacman.ui.fx.util.ResourceManager.fmtMessage;
import static de.amr.games.pacman.ui.fx.util.Ufx.alt;
import static de.amr.games.pacman.ui.fx.util.Ufx.just;

import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.shape.DrawMode;

public class Game3dActions {

	//@formatter:off
	public static final KeyCodeCombination KEY_TOGGLE_DASHBOARD_VISIBLE = just(KeyCode.F1);
	public static final KeyCodeCombination KEY_TOGGLE_DASHBOARD_VISIBLE_2 = alt(KeyCode.B);
	public static final KeyCodeCombination KEY_TOGGLE_PIP_VIEW_VISIBLE = just(KeyCode.F2);
	public static final KeyCodeCombination KEY_TOGGLE_3D_ENABLED = alt(KeyCode.DIGIT3);
	public static final KeyCodeCombination KEY_PREV_CAMERA = alt(KeyCode.LEFT);
	public static final KeyCodeCombination KEY_NEXT_CAMERA = alt(KeyCode.RIGHT);
	//@formatter:on

	private final Game3dUI ui;

	public Game3dActions(Game3dUI ui) {
		this.ui = ui;
	}

	public void togglePipVisibility() {
		var pip = Game3d.ui.pip();
		Ufx.toggle(pip.visiblePy);
		var message = fmtMessage(Game3d.assets.messages, pip.visiblePy.get() ? "pip_on" : "pip_off");
		ui.showFlashMessage(message);
	}

	public void toggleDashboardVisible() {
		Game3d.ui.dashboard().setVisible(!Game3d.ui.dashboard().isVisible());
	}

	public void selectNextPerspective() {
		var nextPerspective = Game3d.d3_perspectivePy.get().next();
		Game3d.d3_perspectivePy.set(nextPerspective);
		String perspectiveName = fmtMessage(Game3d.assets.messages, nextPerspective.name());
		ui.showFlashMessage(fmtMessage(Game3d.assets.messages, "camera_perspective", perspectiveName));
	}

	public void selectPrevPerspective() {
		var prevPerspective = Game3d.d3_perspectivePy.get().prev();
		Game3d.d3_perspectivePy.set(prevPerspective);
		String perspectiveName = fmtMessage(Game3d.assets.messages, prevPerspective.name());
		ui.showFlashMessage(fmtMessage(Game3d.assets.messages, "camera_perspective", perspectiveName));
	}

	public void toggleDrawMode() {
		Game3d.d3_drawModePy.set(Game3d.d3_drawModePy.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
	}
}