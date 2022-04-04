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

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx._2d.scene.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx._3d.scene.Perspective;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.GameLoop;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.layout.VBox;
import javafx.scene.shape.DrawMode;

/**
 * Panel for configuring the game and the UI.
 * 
 * @author Armin Reichert
 */
public class SettingsPanel extends VBox {

	public static class SectionCommands extends InfoSection {
		private ComboBox<GameVariant> comboGameVariant;
		private Button[] btnsSimulation;
		private Button[] btnsGameControl;
		private Spinner<Integer> spinnerLevel;
		private Button btnIntermissionTest;

		public SectionCommands(GameUI ui) {
			super(ui, "Commands");
			btnsSimulation = addButtons("Simulation", "Pause", "Step");
			btnsSimulation[0].setOnAction(e -> ui.togglePaused());
			btnsSimulation[1].setOnAction(e -> GameLoop.get().runSingleStep(true));
			comboGameVariant = addComboBox("Game Variant", GameVariant.MS_PACMAN, GameVariant.PACMAN);
			comboGameVariant.setOnAction(e -> {
				if (comboGameVariant.getValue() != ui.gameController.gameVariant) {
					ui.gameController.selectGameVariant(comboGameVariant.getValue());
				}
			});
			btnsGameControl = addButtons("Game", "Start", "Quit", "Next Level");
			btnsGameControl[0].setOnAction(e -> ui.gameController.requestGame());
			btnsGameControl[1].setOnAction(e -> ui.quitCurrentGameScene());
			btnsGameControl[2].setOnAction(e -> ui.enterNextLevel());
			spinnerLevel = addSpinner("Level", 1, 100, ui.gameController.game.levelNumber);
			spinnerLevel.valueProperty().addListener(($value, oldValue, newValue) -> ui.enterLevel(newValue.intValue()));
			btnIntermissionTest = addButton("Intermission scenes", "Start", ui::startIntermissionScenesTest);
		}

		@Override
		public void update() {
			super.update();
			btnsSimulation[0].setText(Env.$paused.get() ? "Resume" : "Pause");
			btnsSimulation[1].setDisable(!Env.$paused.get());
			comboGameVariant.setValue(ui.gameController.gameVariant);
			comboGameVariant.setDisable(ui.gameController.gameRunning);
			btnsGameControl[0].setDisable(
					ui.gameController.gameRequested || ui.gameController.gameRunning || ui.gameController.attractMode);
			btnsGameControl[1].setDisable(ui.gameController.state == GameState.INTRO);
			btnsGameControl[2].setDisable(ui.gameController.state != GameState.HUNTING);
			spinnerLevel.getValueFactory().setValue(ui.gameController.game.levelNumber);
			spinnerLevel.setDisable(ui.gameController.state != GameState.READY && ui.gameController.state != GameState.HUNTING
					&& ui.gameController.state != GameState.LEVEL_STARTING);
			btnIntermissionTest.setDisable(
					ui.gameController.state == GameState.INTERMISSION_TEST || ui.gameController.state != GameState.INTRO);
		}
	}

	public static class SectionGeneral extends InfoSection {
		private Slider sliderTargetFrameRate;
		private CheckBox cbAutopilot;
		private CheckBox cbImmunity;
		private CheckBox cbUsePlayScene3D;

		public SectionGeneral(GameUI ui) {
			super(ui, "General Settings");
			sliderTargetFrameRate = addSlider("Framerate", 0, 120, 60);
			sliderTargetFrameRate.setSnapToTicks(true);
			sliderTargetFrameRate.setShowTickLabels(true);
			sliderTargetFrameRate.setShowTickMarks(true);
			sliderTargetFrameRate.setMinorTickCount(5);
			sliderTargetFrameRate.setMajorTickUnit(30);
			sliderTargetFrameRate.valueProperty().addListener(($value, _old, _new) -> {
				GameLoop.get().setTargetFrameRate(_new.intValue());
			});
			cbUsePlayScene3D = addCheckBox("Use 3D play scene", ui::toggleUse3DScene);
			cbAutopilot = addCheckBox("Autopilot", ui::toggleAutopilot);
			cbImmunity = addCheckBox("Player immune", ui::toggleImmunity);
		}

		@Override
		public void update() {
			super.update();
			sliderTargetFrameRate.setValue(GameLoop.get().getTargetFrameRate());
			cbAutopilot.setSelected(ui.gameController.autoControlled);
			cbImmunity.setSelected(ui.gameController.game.player.immune);
			cbUsePlayScene3D.setSelected(Env.$3D.get());
		}
	}

	public static class Section3D extends InfoSection {
		private ComboBox<Perspective> comboPerspective;
		private ComboBox<Integer> comboResolution;
		private Slider sliderWallHeight;
		private CheckBox cbUseFloorTexture;
		private CheckBox cbAxesVisible;
		private CheckBox cbWireframeMode;

		public Section3D(GameUI ui) {
			super(ui, "3D Settings");
			comboPerspective = addComboBox("Perspective", Perspective.values());
			comboPerspective.setOnAction(e -> Env.$perspective.set(comboPerspective.getValue()));
			comboResolution = addComboBox("Maze resolution", 1, 2, 4, 8);
			addInfo("Camera", () -> scene3D().getCamController().info()).when(() -> gameScene().is3D());
			comboResolution.setOnAction(e -> Env.$mazeResolution.set(comboResolution.getValue()));
			sliderWallHeight = addSlider("Maze wall height", 0, 10, 8);
			sliderWallHeight.valueProperty().addListener(($value, _old, _new) -> Env.$mazeWallHeight.set(_new.doubleValue()));
			cbUseFloorTexture = addCheckBox("Maze floor texture", ui::toggleUseMazeFloorTexture);
			cbAxesVisible = addCheckBox("Show axes", ui::toggleAxesVisible);
			cbWireframeMode = addCheckBox("Wireframe mode", ui::toggleDrawMode);
		}

		@Override
		public void update() {
			super.update();
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

	public static class Section2D extends InfoSection {
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

	private final SectionCommands sectionCommands;
	private final SectionGeneral sectionGeneral;
	private final Section3D section3D;
	private final Section2D section2D;

	public SettingsPanel(GameUI ui) {
		sectionCommands = new SectionCommands(ui);
		sectionGeneral = new SectionGeneral(ui);
		section3D = new Section3D(ui);
		section2D = new Section2D(ui);
		setVisible(false);
		getChildren().addAll(sectionCommands, sectionGeneral, section3D, section2D);
	}

	public void update() {
		sectionCommands.update();
		sectionGeneral.update();
		section3D.update();
		section2D.update();
	}
}