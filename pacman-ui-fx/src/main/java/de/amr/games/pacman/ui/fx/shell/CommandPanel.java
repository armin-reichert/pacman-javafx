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
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class CommandPanel extends GridPane {

	private final Color textColor = Color.WHITE;
	private final Font textFont = Font.font("Monospace", 14);
	private int row;

	public CommandPanel(GameUI ui) {
		setHgap(20);
		slider("Framerate", 10, 200, 60).valueProperty().addListener(($1, oldVal, newVal) -> {
			ui.setTargetFrameRate(newVal.intValue());
		});
		checkBox("Autopilot", ui::toggleAutopilot);
		checkBox("Immunity", ui::toggleImmunity);
		checkBox("Use 3D scene", ui::toggle3D);
		checkBox("Axes", ui::toggleAxesVisible);
		checkBox("Wireframe Mode", ui::toggleDrawMode);
		checkBox("Tiles", ui::toggleTilesVisible);
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

	private CheckBox checkBox(String text, Runnable callback) {
		CheckBox cb = new CheckBox();
		cb.setTextFill(textColor);
		cb.setFont(textFont);
		cb.setOnAction(e -> callback.run());
		Text label = new Text(text);
		label.setFill(textColor);
		label.setFont(textFont);
		add(label, 0, row);
		add(cb, 1, row++);
		return cb;
	}

	private Slider slider(String text, int min, int max, int value) {
		Slider slider = new Slider(min, max, value);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		slider.setMinorTickCount(5);
		slider.setMajorTickUnit(50);
		Text label = new Text(text);
		label.setFill(textColor);
		label.setFont(textFont);
		add(label, 0, row);
		add(slider, 1, row++);
		return slider;
	}
}