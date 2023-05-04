/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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

import de.amr.games.pacman.ui.fx.app.Actions;
import de.amr.games.pacman.ui.fx.app.AppRes;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.shape.DrawMode;

/**
 * @author Armin Reichert
 */
public class Actions3d {

	public static void togglePipViewVisible() {
		Ufx.toggle(GameApp3d.pipVisiblePy);
		var msgKey = GameApp3d.pipVisiblePy.get() ? "pip_on" : "pip_off";
		Actions.showFlashMessage(AppRes.Texts.message(msgKey));// TODO
	}

	public static void toggleDashboardVisible() {
		Ufx.toggle(GameApp3d.dashboardVisiblePy);
	}

	public static void selectNextPerspective() {
		var nextPerspective = GameApp3d.d3_perspectivePy.get().next();
		GameApp3d.d3_perspectivePy.set(nextPerspective);
		String perspectiveName = AppRes.Texts.message(nextPerspective.name());
		Actions.showFlashMessage(AppRes.Texts.message("camera_perspective", perspectiveName));
	}

	public static void selectPrevPerspective() {
		var prevPerspective = GameApp3d.d3_perspectivePy.get().prev();
		GameApp3d.d3_perspectivePy.set(prevPerspective);
		String perspectiveName = AppRes.Texts.message(prevPerspective.name());
		Actions.showFlashMessage(AppRes.Texts.message("camera_perspective", perspectiveName));
	}

	public static void toggleDrawMode() {
		GameApp3d.d3_drawModePy.set(GameApp3d.d3_drawModePy.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
	}
}