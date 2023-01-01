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
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx.shell.Actions;
import de.amr.games.pacman.ui.fx.shell.GameUI;
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
public class SectionGameControl extends Section {
	private ComboBox<GameVariant> comboGameVariant;
	private Button[] btnsGameControl;
	private Button[] btnsIntermissionTest;
	private Spinner<Integer> spinnerGameLevel;
	private Spinner<Integer> spinnerGameCredit;
	private CheckBox cbMuted;
	private CheckBox cbAutopilot;
	private CheckBox cbImmunity;

	public SectionGameControl(GameUI ui, String title, int minLabelWidth, Color textColor, Font textFont,
			Font labelFont) {
		super(ui, title, minLabelWidth, textColor, textFont, labelFont);

		comboGameVariant = addComboBox("Variant", GameVariant.MS_PACMAN, GameVariant.PACMAN);
		comboGameVariant.setOnAction(e -> {
			if (comboGameVariant.getValue() != gc.game().variant()) {
				gc.state().selectGameVariant(comboGameVariant.getValue());
			}
		});

		btnsGameControl = addButtonList("Game", "Start", "Quit", "Next Level");
		btnsGameControl[0].setOnAction(e -> gc.state().requestGame(gc.game()));
		btnsGameControl[1].setOnAction(e -> Actions.restartIntro());
		btnsGameControl[2].setOnAction(e -> gc.state().cheatEnterNextLevel(gc.game()));

		btnsIntermissionTest = addButtonList("Intermission scenes", "Start", "Quit");
		btnsIntermissionTest[0].setOnAction(e -> Actions.startCutscenesTest());
		btnsIntermissionTest[1].setOnAction(e -> Actions.restartIntro());

		spinnerGameLevel = addSpinner("Level", 1, 100, 1);
		spinnerGameLevel.valueProperty().addListener((obs, oldVal, newVal) -> Actions.enterLevel(newVal.intValue()));
		spinnerGameCredit = addSpinner("Credit", 0, GameModel.MAX_CREDIT, game().credit());
		spinnerGameCredit.valueProperty().addListener((obs, oldVal, newVal) -> game().setCredit(newVal.intValue()));

		cbMuted = addCheckBox("Sound muted", Actions::toggleSoundMuted);
		cbAutopilot = addCheckBox("Autopilot", Actions::toggleAutopilot);
		cbImmunity = addCheckBox("Player immune", Actions::toggleImmunity);

	}

	@Override
	public void update() {
		super.update();

		comboGameVariant.setValue(gc.game().variant());
		comboGameVariant.setDisable(gc.game().isPlaying());

		cbAutopilot.setSelected(gc.isAutoControlled());
		cbImmunity.setSelected(gc.game().isImmune());

		// start game
		btnsGameControl[0].setDisable(!gc.game().hasCredit() || gc.game().isPlaying());
		// quit game
		btnsGameControl[1].setDisable(gc.state() == GameState.INTRO || gc.state() == GameState.INTERMISSION_TEST);
		// next level
		btnsGameControl[2].setDisable(!gc.game().isPlaying() || (gc.state() != GameState.HUNTING
				&& gc.state() != GameState.READY && gc.state() != GameState.CHANGING_TO_NEXT_LEVEL));

		// start intermission test
		btnsIntermissionTest[0].setDisable(gc.state() == GameState.INTERMISSION_TEST || gc.state() != GameState.INTRO);
		// quit intermission test
		btnsIntermissionTest[1].setDisable(gc.state() != GameState.INTERMISSION_TEST);

		game().level().ifPresent(level -> spinnerGameLevel.getValueFactory().setValue(level.number()));
		if (!gc.game().isPlaying() || gc.state() == GameState.CHANGING_TO_NEXT_LEVEL) {
			spinnerGameLevel.setDisable(true);
		} else {
			spinnerGameLevel.setDisable(gc.state() != GameState.READY && gc.state() != GameState.HUNTING
					&& gc.state() != GameState.CHANGING_TO_NEXT_LEVEL);
		}

		spinnerGameCredit.getValueFactory().setValue(game().credit());

		cbMuted.setDisable(false);
		cbMuted.setSelected(gc.sounds().isMuted());
	}
}