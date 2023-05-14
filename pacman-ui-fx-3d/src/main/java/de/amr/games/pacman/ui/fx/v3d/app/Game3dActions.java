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

import de.amr.games.pacman.ui.fx.app.Game2d;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.shape.DrawMode;

public class Game3dActions {

	public void togglePipVisibility() {
		var pip = Game3d.ui.pip();
		Ufx.toggle(pip.visiblePy);
		var message = fmtMessage(Game3d.assets.messages, pip.visiblePy.get() ? "pip_on" : "pip_off");
		Game2d.actions.showFlashMessage(message);
	}

	public void toggleDashboardVisible() {
		Game3d.ui.dashboard().setVisible(!Game3d.ui.dashboard().isVisible());
	}

	public void selectNextPerspective() {
		var nextPerspective = Game3dApplication.d3_perspectivePy.get().next();
		Game3dApplication.d3_perspectivePy.set(nextPerspective);
		String perspectiveName = fmtMessage(Game3d.assets.messages, nextPerspective.name());
		Game2d.actions.showFlashMessage(fmtMessage(Game3d.assets.messages, "camera_perspective", perspectiveName));
	}

	public void selectPrevPerspective() {
		var prevPerspective = Game3dApplication.d3_perspectivePy.get().prev();
		Game3dApplication.d3_perspectivePy.set(prevPerspective);
		String perspectiveName = fmtMessage(Game3d.assets.messages, prevPerspective.name());
		Game2d.actions.showFlashMessage(fmtMessage(Game3d.assets.messages, "camera_perspective", perspectiveName));
	}

	public void toggleDrawMode() {
		Game3dApplication.d3_drawModePy
				.set(Game3dApplication.d3_drawModePy.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
	}
}