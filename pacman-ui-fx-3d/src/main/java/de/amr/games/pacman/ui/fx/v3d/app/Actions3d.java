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

import de.amr.games.pacman.ui.fx.app.Game2d;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.shape.DrawMode;

public class Actions3d extends de.amr.games.pacman.ui.fx.app.Actions2d {

	public void togglePipViewVisible() {
		Ufx.toggle(Game3d.pipVisiblePy);
		var msgKey = Game3d.pipVisiblePy.get() ? "pip_on" : "pip_off";
		Game2d.ACTIONS.showFlashMessage(Game2d.Texts.message(msgKey));// TODO
	}

	public void toggleDashboardVisible() {
		Ufx.toggle(Game3d.dashboardVisiblePy);
	}

	public void selectNextPerspective() {
		var nextPerspective = Game3d.d3_perspectivePy.get().next();
		Game3d.d3_perspectivePy.set(nextPerspective);
		String perspectiveName = Game2d.Texts.message(nextPerspective.name());
		Game2d.ACTIONS.showFlashMessage(Game2d.Texts.message("camera_perspective", perspectiveName));
	}

	public void selectPrevPerspective() {
		var prevPerspective = Game3d.d3_perspectivePy.get().prev();
		Game3d.d3_perspectivePy.set(prevPerspective);
		String perspectiveName = Game2d.Texts.message(prevPerspective.name());
		Game2d.ACTIONS.showFlashMessage(Game2d.Texts.message("camera_perspective", perspectiveName));
	}

	public void toggleDrawMode() {
		Game3d.d3_drawModePy.set(Game3d.d3_drawModePy.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
	}
}