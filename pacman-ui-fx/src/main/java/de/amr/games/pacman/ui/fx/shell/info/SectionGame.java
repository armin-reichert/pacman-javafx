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
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.scene.control.Button;
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

	public SectionGame(GameUI ui, String title, int minLabelWidth, Color textColor, Font textFont, Font labelFont) {
		super(ui, title, minLabelWidth, textColor, textFont, labelFont);
		comboGameVariant = addComboBox("Game Variant", GameVariant.MS_PACMAN, GameVariant.PACMAN);
		comboGameVariant.setOnAction(e -> {
			if (comboGameVariant.getValue() != gc.gameVariant) {
				gc.selectGameVariant(comboGameVariant.getValue());
			}
		});

		btnsGameControl = addButtonList("Game", "Start", "Quit", "Next Level");
		btnsGameControl[0].setOnAction(e -> gc.requestGame());
		btnsGameControl[1].setOnAction(e -> ui.quitCurrentGameScene());
		btnsGameControl[2].setOnAction(e -> ui.enterNextLevel());

		btnsIntermissionTest = addButtonList("Intermission scenes", "Start", "Quit");
		btnsIntermissionTest[0].setOnAction(e -> ui.startIntermissionScenesTest());
		btnsIntermissionTest[1].setOnAction(e -> ui.quitCurrentGameScene());

		spinnerGameLevel = addSpinner("Level", 1, 100, gc.game.levelNumber);
		spinnerGameLevel.valueProperty().addListener(($value, oldValue, newValue) -> ui.enterLevel(newValue.intValue()));
		addInfo("Game State", this::fmtGameState);
		addInfo("", () -> {
			long ticked = gc.stateTimer().ticked();
			return String.format("Running:   %s%s", ticked, gc.stateTimer().isStopped() ? " (STOPPED)" : "");
		});
		addInfo("", () -> {
			long remaining = gc.stateTimer().ticksRemaining();
			String remainingText = remaining == TickTimer.INDEFINITE ? "indefinite" : String.valueOf(remaining);
			return String.format("Remaining: %s", remainingText);
		});
		addInfo("Playing", () -> U.yes_no(gc.gameRunning));
		addInfo("Attract Mode", () -> U.yes_no(gc.attractMode));
		addInfo("Game scene", () -> ui.getCurrentGameScene().getClass().getSimpleName());
		addInfo("", () -> String.format("w=%.0f h=%.0f", ui.getCurrentGameScene().getFXSubScene().getWidth(),
				ui.getCurrentGameScene().getFXSubScene().getHeight()));
		addInfo("Ghost speed", () -> fmtSpeed(gc.game.ghostSpeed));
		addInfo("Ghost speed (frightened)", () -> fmtSpeed(gc.game.ghostSpeedFrightened));
		addInfo("Ghost speed (tunnel)", () -> fmtSpeed(gc.game.ghostSpeedTunnel));
		addInfo("Ghost frightened time", () -> String.format("%d sec", gc.game.ghostFrightenedSeconds));
		addInfo("Pac-Man speed", () -> fmtSpeed(gc.game.playerSpeed));
		addInfo("Pac-Man speed (power)", () -> fmtSpeed(gc.game.playerSpeedPowered));
		addInfo("Bonus value", () -> gc.game.bonusValue(gc.game.bonusSymbol));
		addInfo("Maze flashings", () -> gc.game.numFlashes);
		addInfo("Pellets total",
				() -> String.format("%d (%d energizers)", gc.game.world.pelletsTotal(), gc.game.world.energizersTotal()));
		addInfo("Pellets remaining", () -> gc.game.world.foodRemaining());
	}

	@Override
	public void update() {
		super.update();

		comboGameVariant.setValue(gc.gameVariant);
		comboGameVariant.setDisable(gc.gameRunning);

		// start game
		btnsGameControl[0].setDisable(gc.gameRequested || gc.gameRunning || gc.attractMode);
		// quit game
		btnsGameControl[1].setDisable(gc.state == GameState.INTRO || gc.state == GameState.INTERMISSION_TEST);
		// next level
		btnsGameControl[2].setDisable(!gc.gameRunning
				|| (gc.state != GameState.HUNTING && gc.state != GameState.READY && gc.state != GameState.LEVEL_STARTING));

		// start intermission test
		btnsIntermissionTest[0].setDisable(gc.state == GameState.INTERMISSION_TEST || gc.state != GameState.INTRO);
		// quit intermission test
		btnsIntermissionTest[1].setDisable(gc.state != GameState.INTERMISSION_TEST);

		spinnerGameLevel.getValueFactory().setValue(gc.game.levelNumber);
		if (!gc.gameRunning) {
			spinnerGameLevel.setDisable(true);
		} else {
			spinnerGameLevel.setDisable(
					gc.state != GameState.READY && gc.state != GameState.HUNTING && gc.state != GameState.LEVEL_STARTING);
		}
	}

	private String fmtSpeed(float fraction) {
		return String.format("%.2f px/sec", GameModel.BASE_SPEED * fraction);
	}

	private String huntingPhaseName() {
		return gc.game.inScatteringPhase() ? "Scattering" : "Chasing";
	}

	private String fmtGameState() {
		var game = gc.game;
		var state = gc.state;
		return state == GameState.HUNTING ? //
				String.format("%s: Phase #%d (%s)", state, game.huntingPhase, huntingPhaseName()) : state.name();
	}
}