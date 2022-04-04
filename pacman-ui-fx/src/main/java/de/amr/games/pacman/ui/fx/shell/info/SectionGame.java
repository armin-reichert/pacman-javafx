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
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;

public class SectionGame extends InfoSection {
	private ComboBox<GameVariant> comboGameVariant;
	private Button[] btnsGameControl;
	private Button btnIntermissionTest;
	private Spinner<Integer> spinnerGameLevel;

	public SectionGame(GameUI ui) {
		super(ui, "Game");
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
		btnIntermissionTest = addButton("Intermission scenes", "Start", ui::startIntermissionScenesTest);

		spinnerGameLevel = addSpinner("Level", 1, 100, ui.gameController.game.levelNumber);
		spinnerGameLevel.valueProperty().addListener(($value, oldValue, newValue) -> ui.enterLevel(newValue.intValue()));
		addInfo("Game State", this::fmtGameState);
		addInfo("",
				() -> String.format("Running:   %s%s", stateTimer().ticked(), stateTimer().isStopped() ? " (STOPPED)" : ""));
		addInfo("", () -> String.format("Remaining: %s",
				stateTimer().ticksRemaining() == TickTimer.INDEFINITE ? "indefinite" : stateTimer().ticksRemaining()));
		addInfo("Paused", () -> U.yes_no(Env.$paused.get()));
		addInfo("Playing", () -> U.yes_no(ui.gameController.gameRunning));
		addInfo("Attract Mode", () -> U.yes_no(ui.gameController.attractMode));
		addInfo("Game scene", () -> gameScene().getClass().getSimpleName());
		addInfo("", () -> String.format("w=%.0f h=%.0f", gameScene().getFXSubScene().getWidth(),
				gameScene().getFXSubScene().getHeight()));

		addInfo("Ghost speed", () -> fmtSpeed(game().ghostSpeed));
		addInfo("Ghost speed (frightened)", () -> fmtSpeed(game().ghostSpeedFrightened));
		addInfo("Pac-Man speed", () -> fmtSpeed(game().playerSpeed));
		addInfo("Pac-Man speed (power)", () -> fmtSpeed(game().playerSpeedPowered));
		addInfo("Bonus value", () -> game().bonusValue(game().bonusSymbol));
		addInfo("Maze flashings", () -> game().numFlashes);
	}

	@Override
	public void update() {
		super.update();
		comboGameVariant.setValue(ui.gameController.gameVariant);
		comboGameVariant.setDisable(ui.gameController.gameRunning);

		btnsGameControl[0]
				.setDisable(ui.gameController.gameRequested || ui.gameController.gameRunning || ui.gameController.attractMode);
		btnsGameControl[1].setDisable(ui.gameController.state == GameState.INTRO);
		btnsGameControl[2].setDisable(ui.gameController.state != GameState.HUNTING);
		btnIntermissionTest.setDisable(
				ui.gameController.state == GameState.INTERMISSION_TEST || ui.gameController.state != GameState.INTRO);

		spinnerGameLevel.getValueFactory().setValue(ui.gameController.game.levelNumber);
		spinnerGameLevel.setDisable(ui.gameController.state != GameState.READY
				&& ui.gameController.state != GameState.HUNTING && ui.gameController.state != GameState.LEVEL_STARTING);
	}
}