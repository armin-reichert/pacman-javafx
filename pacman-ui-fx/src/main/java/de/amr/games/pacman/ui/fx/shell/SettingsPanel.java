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
		private Button[] btnsSimulation;
		private Button btnStartGame;
		private Button btnQuitGameScene;
		private Button btnEnterNextLevel;
		private Button btnStartIntermissionTest;

		public void add() {
			addSectionHeader("Commands");
			btnsSimulation = addButtons("Simulation", "Pause", "Step");
			btnsSimulation[0].setOnAction(e -> ui.togglePaused());
			btnsSimulation[1].setOnAction(e -> Env.gameLoop.runSingleStep(true));
			comboGameVariant = addComboBox("Game Variant", GameVariant.MS_PACMAN, GameVariant.PACMAN);
			comboGameVariant.setOnAction(e -> {
				if (comboGameVariant.getValue() != ui.gameController.gameVariant) {
					ui.gameController.selectGameVariant(comboGameVariant.getValue());
				}
			});
			btnStartGame = addButton("Game Play", "Start", ui.gameController::requestGame);
			btnQuitGameScene = addButton("Game Scene", "Quit", ui::quitCurrentGameScene);
			btnEnterNextLevel = addButton("Enter next level", "Next", ui::enterNextLevel);
			btnStartIntermissionTest = addButton("Intermission scenes", "Start", ui::startIntermissionTest);
		}

		public void update() {
			btnsSimulation[0].setText(Env.$paused.get() ? "Resume" : "Pause");
			btnsSimulation[1].setDisable(!Env.$paused.get());
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
		private Slider sliderTargetFrameRate;
		private CheckBox cbAutopilot;
		private CheckBox cbImmunity;
		private CheckBox cbUsePlayScene3D;

		public void add() {
			addSectionHeader("General Settings");
			sliderTargetFrameRate = addSlider("Framerate", 0, 120, 60);
			sliderTargetFrameRate.setSnapToTicks(true);
			sliderTargetFrameRate.setShowTickLabels(true);
			sliderTargetFrameRate.setShowTickMarks(true);
			sliderTargetFrameRate.setMinorTickCount(5);
			sliderTargetFrameRate.setMajorTickUnit(30);
			sliderTargetFrameRate.valueProperty().addListener(($value, _old, _new) -> {
				ui.setTargetFrameRate(_new.intValue());
			});
			cbUsePlayScene3D = addCheckBox("Use 3D play scene", ui::toggleUsePlayScene3D);
			cbAutopilot = addCheckBox("Autopilot", ui::toggleAutopilot);
			cbImmunity = addCheckBox("Player immune", ui::toggleImmunity);
		}

		public void update() {
			sliderTargetFrameRate.setValue(Env.gameLoop.getTargetFrameRate());
			cbAutopilot.setSelected(ui.gameController.autoControlled);
			cbImmunity.setSelected(ui.gameController.game.player.immune);
			cbUsePlayScene3D.setSelected(Env.$3D.get());
		}
	}

	private class Settings3D {
		private ComboBox<Perspective> comboPerspective;
		private ComboBox<Integer> comboResolution;
		private Slider sliderWallHeight;
		private CheckBox cbUseFloorTexture;
		private CheckBox cbAxesVisible;
		private CheckBox cbWireframeMode;

		public void add() {
			addSectionHeader("3D Settings");
			comboPerspective = addComboBox("Perspective", Perspective.values());
			comboPerspective.setOnAction(e -> Env.$perspective.set(comboPerspective.getValue()));
			comboResolution = addComboBox("Maze resolution", 1, 2, 4, 8);
			comboResolution.setOnAction(e -> Env.$mazeResolution.set(comboResolution.getValue()));
			sliderWallHeight = addSlider("Maze wall height", 0, 10, 8);
			sliderWallHeight.valueProperty().addListener(($value, _old, _new) -> Env.$mazeWallHeight.set(_new.doubleValue()));
			cbUseFloorTexture = addCheckBox("Maze floor texture", ui::toggleUseMazeFloorTexture);
			cbAxesVisible = addCheckBox("Show axes", ui::toggleAxesVisible);
			cbWireframeMode = addCheckBox("Wireframe mode", ui::toggleDrawMode);
		}

		public void update() {
			comboPerspective.setValue(Env.$perspective.get());
			comboPerspective.setDisable(!ui.getCurrentGameScene().is3D());
			comboResolution.setValue(Env.$mazeResolution.get());
			comboResolution.setDisable(!ui.getCurrentGameScene().is3D());
			sliderWallHeight.setValue(Env.$mazeWallHeight.get());
			sliderWallHeight.setDisable(!ui.getCurrentGameScene().is3D());
			cbUseFloorTexture.setSelected(Env.$useMazeFloorTexture.get());
			cbUseFloorTexture.setDisable(!ui.getCurrentGameScene().is3D());
			cbAxesVisible.setSelected(Env.$axesVisible.get());
			cbAxesVisible.setDisable(!ui.getCurrentGameScene().is3D());
			cbWireframeMode.setSelected(Env.$drawMode3D.get() == DrawMode.LINE);
			cbWireframeMode.setDisable(!ui.getCurrentGameScene().is3D());
		}
	}

	private class Settings2D {
		private CheckBox cbTilesVisible;

		public void add() {
			addSectionHeader("2D Settings");
			cbTilesVisible = addCheckBox("Show tiles", ui::toggleTilesVisible);
		}

		public void update() {
			cbTilesVisible.setSelected(Env.$tilesVisible.get());
			cbTilesVisible.setDisable(ui.getCurrentGameScene().is3D());
		}
	}

	private final Commands commands = new Commands();
	private final SettingsGeneral settingsGeneral = new SettingsGeneral();
	private final Settings3D settings3D = new Settings3D();
	private final Settings2D settings2D = new Settings2D();

	public SettingsPanel(GameUI ui, int minWidth) {
		this.ui = ui;

		setBackground(U.colorBackground(new Color(0.3, 0.3, 0.3, 0.6)));
		setMinWidth(minWidth);
		setPadding(new Insets(5));
		setHgap(20);
		setVgap(4);
		setVisible(false);

		commands.add();
		settingsGeneral.add();
		settings3D.add();
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
		add(child, 1, row);
		++row;
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

	@SuppressWarnings("unchecked")
	private <T> ComboBox<T> addComboBox(String labelText, T... items) {
		var combo = new ComboBox<T>(FXCollections.observableArrayList(items));
		addRow(labelText, combo);
		return combo;
	}

	private Slider addSlider(String labelText, double min, double max, double value) {
		Slider slider = new Slider(min, max, value);
		slider.setMinWidth(200);
		addRow(labelText, slider);
		return slider;
	}
}