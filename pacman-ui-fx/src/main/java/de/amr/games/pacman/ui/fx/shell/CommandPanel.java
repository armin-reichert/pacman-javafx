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
package de.amr.games.pacman.ui.fx.shell;

import de.amr.games.pacman.ui.fx.app.Env;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class CommandPanel extends GridPane {

	private final Color textColor = Color.WHITE;
	private final Font textFont = Font.font("Monospace", 14);
	private int row;

	public CommandPanel(GameUI ui) {
		row = 0;
		checkBox("Autopilot", ui::toggleAutopilot);
		checkBox("Immunity", ui::toggleImmunity);
		checkBox("Use 2D scene", ui::toggle3D);
		checkBox("Axes", ui::toggleAxesVisible);
		setVisible(false);
	}

	public void show() {
		Env.$paused.set(true);
		setVisible(true);
	}

	public void hide() {
		Env.$paused.set(false);
		setVisible(false);
	}

	private void checkBox(String text, Runnable callback) {
		CheckBox cb = new CheckBox(text);
		cb.setTextFill(textColor);
		cb.setFont(textFont);
		cb.setOnAction(e -> callback.run());
		add(cb, 0, row++);
	}
}