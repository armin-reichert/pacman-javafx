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

import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Game related settings.
 * 
 * @author Armin Reichert
 */
public class SectionGame extends Section {
	private ComboBox<GameVariant> comboGameVariant;
	private Button[] btnsGameControl;
	private Button[] btnsIntermissionTest;
	private Spinner<Integer> spinnerGameLevel;
	private CheckBox cbAutopilot;
	private CheckBox cbImmunity;

	public SectionGame(GameUI ui, String title, int minLabelWidth, Color textColor, Font textFont, Font labelFont) {
		super(ui, title, minLabelWidth, textColor, textFont, labelFont);

		var game = gameController.game();
		var gameState = gameController.state();
		var gameVariant = gameController.gameVariant();

		comboGameVariant = addComboBox("Game Variant", GameVariant.MS_PACMAN, GameVariant.PACMAN);
		comboGameVariant.setOnAction(e -> {
			if (comboGameVariant.getValue() != gameVariant) {
				gameController.selectGameVariant(comboGameVariant.getValue());
			}
		});

		btnsGameControl = addButtonList("Game", "Start", "Quit", "Next Level");
		btnsGameControl[0].setOnAction(e -> gameController.requestGame());
		btnsGameControl[1].setOnAction(e -> ui.quitCurrentScene());
		btnsGameControl[2].setOnAction(e -> gameController.cheatEnterNextLevel());

		btnsIntermissionTest = addButtonList("Intermission scenes", "Start", "Quit");
		btnsIntermissionTest[0].setOnAction(e -> ui.startIntermissionScenesTest());
		btnsIntermissionTest[1].setOnAction(e -> ui.quitCurrentScene());

		spinnerGameLevel = addSpinner("Level", 1, 100, game.levelNumber);
		spinnerGameLevel.valueProperty().addListener(($value, oldValue, newValue) -> ui.enterLevel(newValue.intValue()));

		cbAutopilot = addCheckBox("Autopilot", ui::toggleAutopilot);
		cbImmunity = addCheckBox("Player immune", ui::toggleImmunity);

		addInfo("Game State", () -> fmtGameState(game, gameState));
		addInfo("", () -> {
			long ticked = gameState.timer().tick();
			return String.format("Running:   %s%s", ticked, gameState.timer().isStopped() ? " (STOPPED)" : "");
		});
		addInfo("", () -> {
			long remaining = gameState.timer().remaining();
			String remainingText = remaining == TickTimer.INDEFINITE ? "indefinite" : String.valueOf(remaining);
			return String.format("Remaining: %s", remainingText);
		});
		addInfo("Credit", () -> "%d".formatted(gameController.credit()));
		addInfo("Playing", () -> U.yes_no(gameController.gameRunning));
		addInfo("Game scene", () -> ui.getCurrentGameScene().getClass().getSimpleName());
		addInfo("", () -> String.format("w=%.0f h=%.0f", ui.getCurrentGameScene().getFXSubScene().getWidth(),
				ui.getCurrentGameScene().getFXSubScene().getHeight()));
		addInfo("Ghost speed", () -> fmtSpeed(game.level.ghostSpeed));
		addInfo("Ghost speed (frightened)", () -> fmtSpeed(game.level.ghostSpeedFrightened));
		addInfo("Ghost speed (tunnel)", () -> fmtSpeed(game.level.ghostSpeedTunnel));
		addInfo("Ghost frightened time", () -> String.format("%d sec", game.level.ghostFrightenedSeconds));
		addInfo("Pac-Man speed", () -> fmtSpeed(game.level.playerSpeed));
		addInfo("Pac-Man speed (power)", () -> fmtSpeed(game.level.playerSpeedPowered));
		addInfo("Bonus value", () -> game.bonusValue(game.level.bonusSymbol));
		addInfo("Maze flashings", () -> game.level.numFlashes);
		addInfo("Pellets", () -> String.format("%d of %d (%d energizers)", game.world.foodRemaining(),
				game.world.tiles().filter(game.world::isFoodTile).count(), game.world.energizerTiles().count()));
	}

	@Override
	public void update() {
		super.update();

		var game = gameController.game();
		var gameState = gameController.state();
		var gameVariant = gameController.gameVariant();

		comboGameVariant.setValue(gameVariant);
		comboGameVariant.setDisable(gameController.gameRunning);

		cbAutopilot.setSelected(game.player.autoMoving);
		cbImmunity.setSelected(game.player.immune);

		// start game
		btnsGameControl[0].setDisable(gameController.credit() == 0 || gameController.gameRunning);
		// quit game
		btnsGameControl[1].setDisable(gameState == GameState.INTRO || gameState == GameState.INTERMISSION_TEST);
		// next level
		btnsGameControl[2].setDisable(!gameController.gameRunning
				|| (gameState != GameState.HUNTING && gameState != GameState.READY && gameState != GameState.LEVEL_STARTING));

		// start intermission test
		btnsIntermissionTest[0].setDisable(gameState == GameState.INTERMISSION_TEST || gameState != GameState.INTRO);
		// quit intermission test
		btnsIntermissionTest[1].setDisable(gameState != GameState.INTERMISSION_TEST);

		spinnerGameLevel.getValueFactory().setValue(game.levelNumber);
		if (!gameController.gameRunning) {
			spinnerGameLevel.setDisable(true);
		} else {
			spinnerGameLevel.setDisable(
					gameState != GameState.READY && gameState != GameState.HUNTING && gameState != GameState.LEVEL_STARTING);
		}
	}

	private String fmtSpeed(float fraction) {
		return String.format("%.2f px/sec", GameModel.BASE_SPEED * fraction);
	}

	private String fmtGameState(GameModel game, GameState gameState) {
		var huntingPhaseName = game.inScatteringPhase() ? "Scattering" : "Chasing";
		return gameState == GameState.HUNTING ? //
				String.format("%s: Phase #%d (%s)", gameState, game.huntingPhase, huntingPhaseName) : gameState.name();
	}
}