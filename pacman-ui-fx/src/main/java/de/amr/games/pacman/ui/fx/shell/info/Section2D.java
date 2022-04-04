/*
MIT License

Copyright (c) 2021 Armin Reichert

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
package de.amr.games.pacman.ui.fx.shell.info;

import de.amr.games.pacman.ui.fx._2d.scene.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import javafx.scene.control.CheckBox;

public class Section2D extends InfoSection {
	private CheckBox cbTilesVisible;

	public Section2D(GameUI ui) {
		super(ui, "2D Settings");
		addInfo("Canvas2D", () -> {
			AbstractGameScene2D scene2D = (AbstractGameScene2D) ui.getCurrentGameScene();
			return String.format("w=%.0f h=%.0f", scene2D.getCanvas().getWidth(), scene2D.getCanvas().getHeight());
		}).when(() -> !gameScene().is3D());
		cbTilesVisible = addCheckBox("Show tiles", ui::toggleTilesVisible);
	}

	@Override
	public void update() {
		super.update();
		cbTilesVisible.setSelected(Env.$tilesVisible.get());
		cbTilesVisible.setDisable(ui.getCurrentGameScene().is3D());
	}
}