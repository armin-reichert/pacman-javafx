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

import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx._3d.scene.Perspective;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class CommandPanel extends GridPane {

	private final GameUI ui;
	private final Color textColor = Color.WHITE;
	private final Font textFont = Font.font("Monospace", 14);
	private final Color headerColor = Color.YELLOW;
	private final Font headerFont = Font.font("Monospace", FontWeight.BOLD, 18);
	private int row;

	private final Slider sliderTargetFrameRate;
	private final ComboBox<GameVariant> comboGameVariant;
	private final CheckBox cbAutopilot;
	private final CheckBox cbImmunity;
	private final CheckBox cbUse3DScene;
	private final ComboBox<Perspective> comboPerspective;
	private final ComboBox<Integer> comboMazeResolution;
	private final CheckBox cbUseMazeFloorTexture;
	private final CheckBox cbAxesVisible;
	private final CheckBox cbWireframeMode;
	private final CheckBox cbShowTiles;

	public CommandPanel(GameUI ui, int width) {
		this.ui = ui;
		setBackground(U.colorBackground(new Color(0.3, 0.3, 0.3, 0.6)));
		setHgap(20);
		setMinWidth(width);
		setWidth(width);
		setMaxWidth(width);

		sliderTargetFrameRate = addSlider("Framerate", 10, 200, 60);
		sliderTargetFrameRate.valueProperty().addListener(($1, oldVal, newVal) -> {
			ui.setTargetFrameRate(newVal.intValue());
		});

		addSectionHeader("General settings");
		comboGameVariant = addComboBox("GameVariant", GameVariant.MS_PACMAN, GameVariant.PACMAN);
		comboGameVariant.setOnAction(e -> ui.gameController.selectGameVariant(comboGameVariant.getValue()));
		cbUse3DScene = addCheckBox("Use 3D scene", ui::toggle3D);
		cbAutopilot = addCheckBox("Autopilot", ui::toggleAutopilot);
		cbImmunity = addCheckBox("Player immune", ui::toggleImmunity);

		addSectionHeader("3D settings");
		comboPerspective = addComboBox("Perspective", Perspective.values());
		comboPerspective.setOnAction(e -> Env.$perspective.set(comboPerspective.getValue()));
		comboMazeResolution = addComboBox("Maze resolution", 1, 2, 4, 8);
		comboMazeResolution.setOnAction(e -> Env.$mazeResolution.set(comboMazeResolution.getValue()));
		cbUseMazeFloorTexture = addCheckBox("Maze floor texture", ui::toggleUseMazeFloorTexture);
		cbAxesVisible = addCheckBox("Show Axes", ui::toggleAxesVisible);
		cbWireframeMode = addCheckBox("Wireframe Mode", ui::toggleDrawMode);

		addSectionHeader("2D settings");
		cbShowTiles = addCheckBox("Show Tiles", ui::toggleTilesVisible);
		setVisible(false);
	}

	public void update() {
		sliderTargetFrameRate.setValue(Env.gameLoop.getTargetFrameRate());
		comboGameVariant.setValue(ui.gameController.gameVariant);
		cbAutopilot.setSelected(ui.gameController.autoControlled);
		cbImmunity.setSelected(ui.gameController.game.player.immune);
		cbUse3DScene.setSelected(Env.$3D.get());
		comboPerspective.setValue(Env.$perspective.get());
		comboMazeResolution.setValue(Env.$mazeResolution.get());
		cbUseMazeFloorTexture.setSelected(Env.$useMazeFloorTexture.get());
		cbAxesVisible.setSelected(Env.$axesVisible.get());
		cbWireframeMode.setSelected(Env.$drawMode3D.get() == DrawMode.LINE);
		cbShowTiles.setSelected(Env.$tilesVisible.get());
	}

	private void addRow(String labelText, Control control) {
		Text label = new Text(labelText);
		label.setFill(textColor);
		label.setFont(textFont);
		add(label, 0, row);
		add(control, 1, row++);
	}

	private void addSectionHeader(String title) {
		Text header = new Text(title);
		header.setFill(headerColor);
		header.setFont(headerFont);
		header.setTextAlignment(TextAlignment.CENTER);
		setConstraints(header, 0, row, 2, 1, HPos.CENTER, VPos.CENTER, Priority.NEVER, Priority.NEVER, new Insets(10));
		getChildren().add(header);
		++row;
	}

	private CheckBox addCheckBox(String labelText, Runnable callback) {
		CheckBox cb = new CheckBox();
		cb.setTextFill(textColor);
		cb.setFont(textFont);
		cb.setOnAction(e -> callback.run());
		addRow(labelText, cb);
		return cb;
	}

	private Slider addSlider(String labelText, int min, int max, int value) {
		Slider slider = new Slider(min, max, value);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		slider.setMinorTickCount(5);
		slider.setMajorTickUnit(50);
		slider.setMinWidth(200);
		addRow(labelText, slider);
		return slider;
	}

	@SuppressWarnings("unchecked")
	private <T> ComboBox<T> addComboBox(String labelText, T... items) {
		var combo = new ComboBox<T>();
		for (T item : items) {
			combo.getItems().add(item);
		}
		addRow(labelText, combo);
		return combo;
	}
}