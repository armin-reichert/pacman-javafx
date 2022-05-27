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

import static de.amr.games.pacman.lib.TickTimer.ticksAsString;

import de.amr.games.pacman.controller.common.GameState;
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

	private static String fmtSpeed(float fraction) {
		return String.format("%.2f px/sec", GameModel.BASE_SPEED * fraction);
	}

	public SectionGame(GameUI ui, String title, int minLabelWidth, Color textColor, Font textFont, Font labelFont) {
		super(ui, title, minLabelWidth, textColor, textFont, labelFont);

		comboGameVariant = addComboBox("Game Variant", GameVariant.MS_PACMAN, GameVariant.PACMAN);
		comboGameVariant.setOnAction(e -> {
			if (comboGameVariant.getValue() != gc.gameVariant()) {
				gc.selectGameVariant(comboGameVariant.getValue());
			}
		});

		btnsGameControl = addButtonList("Game", "Start", "Quit", "Next Level");
		btnsGameControl[0].setOnAction(e -> gc.requestGame());
		btnsGameControl[1].setOnAction(e -> ui.quitCurrentScene());
		btnsGameControl[2].setOnAction(e -> gc.cheatEnterNextLevel());

		btnsIntermissionTest = addButtonList("Intermission scenes", "Start", "Quit");
		btnsIntermissionTest[0].setOnAction(e -> ui.startIntermissionScenesTest());
		btnsIntermissionTest[1].setOnAction(e -> ui.quitCurrentScene());

		spinnerGameLevel = addSpinner("Level", 1, 100, game().levelNumber);
		spinnerGameLevel.valueProperty().addListener(($value, oldValue, newValue) -> ui.enterLevel(newValue.intValue()));

		cbAutopilot = addCheckBox("Autopilot", ui::toggleAutopilot);
		cbImmunity = addCheckBox("Player immune", ui::toggleImmunity);

		addInfo("Game scene", () -> ui.getCurrentGameScene().getClass().getSimpleName());
		addInfo("", () -> "w=%.0f h=%.0f".formatted(ui.getCurrentGameScene().getFXSubScene().getWidth(),
				ui.getCurrentGameScene().getFXSubScene().getHeight()));
		addInfo("Game State", () -> "%s".formatted(gc.state()));
		addInfo("", () -> "Running:   %s%s".formatted(gc.state().timer().tick(),
				gc.state().timer().isStopped() ? " (STOPPED)" : ""));
		addInfo("", () -> "Remaining: %s".formatted(ticksAsString(gc.state().timer().remaining())));

		addInfo("Hunting timer",
				() -> "%s #%d%s".formatted(gc.huntingTimer().phaseName(),
						gc.huntingTimer().scatteringPhase() != -1 ? gc.huntingTimer().scatteringPhase()
								: gc.huntingTimer().chasingPhase(),
						gc.huntingTimer().isStopped() ? " STOPPED" : ""));
		addInfo("", () -> "Running:   %d".formatted(gc.huntingTimer().tick()));
		addInfo("", () -> "Remaining: %s".formatted(ticksAsString(gc.huntingTimer().remaining())));

		addInfo("Credit", () -> "%d".formatted(gc.credit()));
		addInfo("Playing", () -> U.yes_no(gc.isGameRunning()));

		addInfo("Pellets",
				() -> String.format("%d of %d (%d energizers)", game().level.world.foodRemaining(),
						game().level.world.tiles().filter(game().level.world::isFoodTile).count(),
						game().level.world.energizerTiles().count()));
		addInfo("Ghost speed", () -> fmtSpeed(game().level.ghostSpeed));
		addInfo("Ghost speed (frightened)", () -> fmtSpeed(game().level.ghostSpeedFrightened));
		addInfo("Ghost speed (tunnel)", () -> fmtSpeed(game().level.ghostSpeedTunnel));
		addInfo("Ghost frightened time", () -> String.format("%d sec", game().level.ghostFrightenedSeconds));
		addInfo("Pac-Man speed", () -> fmtSpeed(game().level.playerSpeed));
		addInfo("Pac-Man speed (power)", () -> fmtSpeed(game().level.playerSpeedPowered));
		addInfo("Bonus value", () -> game().bonusValue(game().level.bonusSymbol));
		addInfo("Maze flashings", () -> game().level.numFlashes);
	}

	@Override
	public void update() {
		super.update();

		comboGameVariant.setValue(gc.gameVariant());
		comboGameVariant.setDisable(gc.isGameRunning());

		cbAutopilot.setSelected(gc.isAutoMoving());
		cbImmunity.setSelected(gc.isPlayerImmune());

		// start game
		btnsGameControl[0].setDisable(gc.credit() == 0 || gc.isGameRunning());
		// quit game
		btnsGameControl[1].setDisable(gc.state() == GameState.INTRO || gc.state() == GameState.INTERMISSION_TEST);
		// next level
		btnsGameControl[2].setDisable(!gc.isGameRunning() || (gc.state() != GameState.HUNTING
				&& gc.state() != GameState.READY && gc.state() != GameState.LEVEL_STARTING));

		// start intermission test
		btnsIntermissionTest[0].setDisable(gc.state() == GameState.INTERMISSION_TEST || gc.state() != GameState.INTRO);
		// quit intermission test
		btnsIntermissionTest[1].setDisable(gc.state() != GameState.INTERMISSION_TEST);

		spinnerGameLevel.getValueFactory().setValue(game().levelNumber);
		if (!gc.isGameRunning()) {
			spinnerGameLevel.setDisable(true);
		} else {
			spinnerGameLevel.setDisable(
					gc.state() != GameState.READY && gc.state() != GameState.HUNTING && gc.state() != GameState.LEVEL_STARTING);
		}
	}
}