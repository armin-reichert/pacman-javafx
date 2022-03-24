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
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class CommandPanel extends GridPane {

	private final GameUI ui;
	private final Color textColor = Color.WHITE;
	private final Font textFont = Font.font("Monospace", 14);
	private int row;

	private final CheckBox cbAutopilot;
	private final CheckBox cbImmunity;
	private final CheckBox cbUse3DScene;
	private final CheckBox cbUseMazeFloorTexture;
	private final CheckBox cbAxesVisible;
	private final CheckBox cbWireframeMode;
	private final CheckBox cbShowTiles;

	public CommandPanel(GameUI ui) {
		this.ui = ui;
		setHgap(20);
		addSlider("Framerate", 10, 200, 60).valueProperty().addListener(($1, oldVal, newVal) -> {
			ui.setTargetFrameRate(newVal.intValue());
		});
		cbAutopilot = addCheckBox("Autopilot", ui::toggleAutopilot);
		cbImmunity = addCheckBox("Player immune", ui::toggleImmunity);
		cbUse3DScene = addCheckBox("Use 3D scene", ui::toggle3D);
		cbUseMazeFloorTexture = addCheckBox("Maze floor texture", ui::toggleUseMazeFloorTexture);
		cbAxesVisible = addCheckBox("Show Axes", ui::toggleAxesVisible);
		cbWireframeMode = addCheckBox("Wireframe Mode", ui::toggleDrawMode);
		cbShowTiles = addCheckBox("Tiles", ui::toggleTilesVisible);
		visibleProperty().addListener(this::onVisibilityChange);
		setVisible(false);
	}

	private void onVisibilityChange(ObservableValue<? extends Boolean> $1, Boolean wasVisible, Boolean becomesVisible) {
		if (becomesVisible) {
			Env.$paused.set(true);
			cbAutopilot.setSelected(ui.gameController.autoControlled);
			cbImmunity.setSelected(ui.gameController.game.player.immune);
			cbUse3DScene.setSelected(Env.$3D.get());
			cbUseMazeFloorTexture.setSelected(Env.$useMazeFloorTexture.get());
			cbAxesVisible.setSelected(Env.$axesVisible.get());
			cbWireframeMode.setSelected(Env.$drawMode3D.get() == DrawMode.LINE);
			cbShowTiles.setSelected(Env.$tilesVisible.get());
		} else {
			Env.$paused.set(false);
		}
	}

	private CheckBox addCheckBox(String text, Runnable callback) {
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

	private Slider addSlider(String text, int min, int max, int value) {
		Slider slider = new Slider(min, max, value);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		slider.setMinorTickCount(5);
		slider.setMajorTickUnit(50);
		slider.setMinWidth(250);
		Text label = new Text(text);
		label.setFill(textColor);
		label.setFont(textFont);
		add(label, 0, row);
		add(slider, 1, row++);
		return slider;
	}
}