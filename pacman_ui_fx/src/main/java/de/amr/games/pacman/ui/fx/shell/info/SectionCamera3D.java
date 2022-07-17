/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
import de.amr.games.pacman.ui.fx._3d.scene.cams.Perspective;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * 3D related settings.
 * 
 * @author Armin Reichert
 */
public class SectionCamera3D extends Section {

	private final ComboBox<Perspective> comboPerspective;

	public SectionCamera3D(GameUI ui, GameController gc, String title, int minLabelWidth, Color textColor, Font textFont,
			Font labelFont) {
		super(ui, gc, title, minLabelWidth, textColor, textFont, labelFont);

		comboPerspective = addComboBox("Perspective", Perspective.values());
		comboPerspective.setOnAction(e -> Env.perspectivePy.set(comboPerspective.getValue()));

		addInfo("Camera",
				() -> (gameScene() instanceof PlayScene3D playScene3D) ? playScene3D.getCamera().transformInfo() : "")
						.available(() -> gameScene().is3D());
		addInfo("Shift+LEFT/RIGHT", "Camera -X / +X").available(() -> Env.perspectivePy.get() == Perspective.TOTAL);
		addInfo("Shift+PLUS/MINUS", "Camera -Y / +Y").available(() -> Env.perspectivePy.get() == Perspective.TOTAL);
		addInfo("Shift+UP/DOWN", "Camera -Z / +Z")
				.available(() -> Env.perspectivePy.get() == Perspective.TOTAL || Env.perspectivePy.get() == Perspective.DRONE);
		addInfo("Ctrl+Shift+UP/DOWN", "Camera Rotate X").available(() -> Env.perspectivePy.get() == Perspective.TOTAL);
	}

	@Override
	public void update() {
		super.update();
		comboPerspective.setValue(Env.perspectivePy.get());
		comboPerspective.setDisable(!gameScene().is3D());
	}
}