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

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx._3d.scene.Perspective;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 * Panel for configuring the game and the UI.
 * 
 * @author Armin Reichert
 */
public class SettingsPanel extends GridPane {

	private final GameUI ui;
	private final Color textColor = Color.WHITE;
	private final Font textFont = Font.font("Monospace", 14);
	private final Color headerColor = Color.YELLOW;
	private final Font headerFont = Font.font("Monospace", FontWeight.BOLD, 14);
	private int row;

	private class Commands {
		private ComboBox<GameVariant> comboGameVariant;
		private Button btnStartGame;
		private Button btnQuitGameScene;
		private Button btnEnterNextLevel;
		private Button btnStartIntermissionTest;

		public void add() {
			comboGameVariant = addComboBox("Game Variant", GameVariant.MS_PACMAN, GameVariant.PACMAN);
			comboGameVariant.setOnAction(e -> ui.gameController.selectGameVariant(comboGameVariant.getValue()));
			btnStartGame = addButton("Game", "Start", () -> ui.gameController.requestGame());
			btnQuitGameScene = addButton("Game Scene", "Quit", () -> ui.quitCurrentGameScene());
			btnEnterNextLevel = addButton("Enter next level", "Next", () -> ui.enterNextLevel());
			btnStartIntermissionTest = addButton("Intermission scenes", "Start", () -> ui.startIntermissionTest());
		}

		public void update() {
			comboGameVariant.setValue(ui.gameController.gameVariant);
			comboGameVariant.setDisable(ui.gameController.gameRunning);
			btnStartGame.setDisable(
					ui.gameController.gameRequested || ui.gameController.gameRunning || ui.gameController.attractMode);
			btnQuitGameScene.setDisable(ui.gameController.state == GameState.INTRO);
			btnStartIntermissionTest.setDisable(
					ui.gameController.state == GameState.INTERMISSION_TEST || ui.gameController.state != GameState.INTRO);
			btnEnterNextLevel.setDisable(!ui.gameController.gameRunning);
		}
	}

	private class SettingsGeneral {
		private CheckBox cbGamePaused;
		private Slider sliderTargetFrameRate;
		private CheckBox cbAutopilot;
		private CheckBox cbImmunity;
		private CheckBox cbUse3DScene;

		public void add() {
			cbGamePaused = addCheckBox("Game paused", () -> ui.togglePaused());
			sliderTargetFrameRate = addSlider("Framerate", 10, 200, 60);
			sliderTargetFrameRate.setShowTickLabels(true);
			sliderTargetFrameRate.setShowTickMarks(true);
			sliderTargetFrameRate.setMinorTickCount(5);
			sliderTargetFrameRate.setMajorTickUnit(50);
			sliderTargetFrameRate.valueProperty().addListener(($1, oldVal, newVal) -> {
				ui.setTargetFrameRate(newVal.intValue());
			});
			cbUse3DScene = addCheckBox("Use 3D play scene", ui::toggle3D);
			cbAutopilot = addCheckBox("Autopilot", ui::toggleAutopilot);
			cbImmunity = addCheckBox("Player immune", ui::toggleImmunity);
		}

		public void update() {
			cbGamePaused.setSelected(Env.$paused.get());
			sliderTargetFrameRate.setValue(Env.gameLoop.getTargetFrameRate());
			cbAutopilot.setSelected(ui.gameController.autoControlled);
			cbImmunity.setSelected(ui.gameController.game.player.immune);
			cbUse3DScene.setSelected(Env.$3D.get());
		}
	}

	private class Settings3D {
		private ComboBox<Perspective> comboPerspective;
		private ComboBox<Integer> comboMazeResolution;
		private Slider sliderMazeWallHeight;
		private CheckBox cbUseMazeFloorTexture;
		private CheckBox cbAxesVisible;
		private CheckBox cbWireframeMode;

		public void add() {
			comboPerspective = addComboBox("Perspective", Perspective.values());
			comboPerspective.setOnAction(e -> Env.$perspective.set(comboPerspective.getValue()));
			comboMazeResolution = addComboBox("Maze resolution", 1, 2, 4, 8);
			comboMazeResolution.setOnAction(e -> Env.$mazeResolution.set(comboMazeResolution.getValue()));
			sliderMazeWallHeight = addSlider("Maze wall height", 0, 10, 8);
			sliderMazeWallHeight.valueProperty().addListener(($1, oldVal, newVal) -> {
				Env.$mazeWallHeight.set(newVal.doubleValue());
			});
			cbUseMazeFloorTexture = addCheckBox("Maze floor texture", ui::toggleUseMazeFloorTexture);
			cbAxesVisible = addCheckBox("Show axes", ui::toggleAxesVisible);
			cbWireframeMode = addCheckBox("Wireframe mode", ui::toggleDrawMode);
		}

		public void update() {
			comboPerspective.setValue(Env.$perspective.get());
			comboPerspective.setDisable(!ui.getCurrentGameScene().is3D());
			comboMazeResolution.setValue(Env.$mazeResolution.get());
			comboMazeResolution.setDisable(!ui.getCurrentGameScene().is3D());
			sliderMazeWallHeight.setValue(Env.$mazeWallHeight.get());
			sliderMazeWallHeight.setDisable(!ui.getCurrentGameScene().is3D());
			cbUseMazeFloorTexture.setSelected(Env.$useMazeFloorTexture.get());
			cbUseMazeFloorTexture.setDisable(!ui.getCurrentGameScene().is3D());
			cbAxesVisible.setSelected(Env.$axesVisible.get());
			cbAxesVisible.setDisable(!ui.getCurrentGameScene().is3D());
			cbWireframeMode.setSelected(Env.$drawMode3D.get() == DrawMode.LINE);
			cbWireframeMode.setDisable(!ui.getCurrentGameScene().is3D());
		}
	}

	private class Settings2D {
		private CheckBox cbShowTiles;

		public void add() {
			cbShowTiles = addCheckBox("Show tiles", ui::toggleTilesVisible);
		}

		public void update() {
			cbShowTiles.setSelected(Env.$tilesVisible.get());
			cbShowTiles.setDisable(ui.getCurrentGameScene().is3D());
		}
	}

	private Commands commands = new Commands();
	private SettingsGeneral settingsGeneral = new SettingsGeneral();
	private Settings3D settings3D = new Settings3D();
	private Settings2D settings2D = new Settings2D();

	public SettingsPanel(GameUI ui, int width) {
		this.ui = ui;

		setBackground(U.colorBackground(new Color(0.3, 0.3, 0.3, 0.6)));
		setHgap(20);
		setVgap(4);
		setMinWidth(width);
		setWidth(width);
		setMaxWidth(width);
		setVisible(false);

		addSectionHeader("Commands");
		commands.add();
		addSectionHeader("General Settings");
		settingsGeneral.add();
		addSectionHeader("3D Settings");
		settings3D.add();
		addSectionHeader("2D Settings");
		settings2D.add();
	}

	public void update() {
		commands.update();
		settingsGeneral.update();
		settings3D.update();
		settings2D.update();
	}

	private void addRow(String labelText, Node child) {
		Text label = new Text(labelText);
		label.setFill(textColor);
		label.setFont(textFont);
		add(label, 0, row);
		add(child, 1, row++);
	}

	private void addSectionHeader(String title) {
		Text header = new Text(title);
		header.setFill(headerColor);
		header.setFont(headerFont);
		header.setTextAlignment(TextAlignment.CENTER);
		setConstraints(header, 0, row, 2, 1, HPos.CENTER, VPos.CENTER, Priority.NEVER, Priority.NEVER, new Insets(8));
		getChildren().add(header);
		++row;
	}

	private Button addButton(String labelText, String buttonText, Runnable action) {
		Button button = new Button(buttonText);
		button.setOnAction(e -> action.run());
		addRow(labelText, button);
		return button;
	}

	private Button[] addButtons(String labelText, String... buttonTexts) {
		HBox hbox = new HBox();
		Button[] buttons = new Button[buttonTexts.length];
		for (int i = 0; i < buttonTexts.length; ++i) {
			buttons[i] = new Button(buttonTexts[i]);
			hbox.getChildren().add(buttons[i]);
		}
		addRow(labelText, hbox);
		return buttons;
	}

	private CheckBox addCheckBox(String labelText, Runnable callback) {
		CheckBox cb = new CheckBox();
		cb.setTextFill(textColor);
		cb.setFont(textFont);
		cb.setOnAction(e -> callback.run());
		addRow(labelText, cb);
		return cb;
	}

	private Slider addSlider(String labelText, double min, double max, double value) {
		Slider slider = new Slider(min, max, value);
		slider.setMinWidth(200);
		addRow(labelText, slider);
		return slider;
	}

	@SuppressWarnings("unchecked")
	private <T> ComboBox<T> addComboBox(String labelText, T... items) {
		var combo = new ComboBox<T>(FXCollections.observableArrayList(items));
		addRow(labelText, combo);
		return combo;
	}
}