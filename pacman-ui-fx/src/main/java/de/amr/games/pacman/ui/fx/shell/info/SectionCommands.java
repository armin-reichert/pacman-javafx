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
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.GameLoop;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;

public class SectionCommands extends InfoSection {
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
		btnsGameControl[0]
				.setDisable(ui.gameController.gameRequested || ui.gameController.gameRunning || ui.gameController.attractMode);
		btnsGameControl[1].setDisable(ui.gameController.state == GameState.INTRO);
		btnsGameControl[2].setDisable(ui.gameController.state != GameState.HUNTING);
		spinnerLevel.getValueFactory().setValue(ui.gameController.game.levelNumber);
		spinnerLevel.setDisable(ui.gameController.state != GameState.READY && ui.gameController.state != GameState.HUNTING
				&& ui.gameController.state != GameState.LEVEL_STARTING);
		btnIntermissionTest.setDisable(
				ui.gameController.state == GameState.INTERMISSION_TEST || ui.gameController.state != GameState.INTRO);
	}
}