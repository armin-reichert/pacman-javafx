/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.app.PacManGames2dUI;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;

/**
 * Game related settings.
 * 
 * @author Armin Reichert
 */
public class SectionGameControl extends Section {

	private static final int GAME_LEVEL_START = 0;
	private static final int GAME_LEVEL_QUIT = 1;
	private static final int GAME_LEVEL_NEXT = 2;

	private static final int INTERMISSION_TEST_START = 0;
	private static final int INTERMISSION_TEST_QUIT = 1;

	private ComboBox<GameVariant> comboGameVariant;
	private ComboBox<Integer> comboInitialLives;
	private Button[] blGameLevel;
	private Button[] blIntermissionTest;
	private Spinner<Integer> spGameLevel;
	private Spinner<Integer> spGameCredit;
	private CheckBox cbAutopilot;
	private CheckBox cbImmunity;

	public SectionGameControl(PacManGames2dUI ui, String title) {
		super(ui, title, Dashboard.MIN_LABEL_WIDTH, Dashboard.TEXT_COLOR, Dashboard.TEXT_FONT, Dashboard.LABEL_FONT);

		comboGameVariant = addComboBox("Variant", GameVariant.MS_PACMAN, GameVariant.PACMAN);
		comboGameVariant.setOnAction(e -> {
			if (comboGameVariant.getValue() != game().variant()) {
				GameController.it().selectGameVariant(comboGameVariant.getValue());
			}
		});

		comboInitialLives = addComboBox("Initial Lives", 3, 5);
		comboInitialLives.setOnAction(e -> game().setInitialLives(comboInitialLives.getValue()));

		blGameLevel = addButtonList("Game Level", "Start", "Quit", "Next");
		blGameLevel[GAME_LEVEL_START].setOnAction(e -> GameController.it().startPlaying());
		blGameLevel[GAME_LEVEL_QUIT].setOnAction(e -> ui.restartIntro());
		blGameLevel[GAME_LEVEL_NEXT].setOnAction(e -> GameController.it().cheatEnterNextLevel());

		blIntermissionTest = addButtonList("Cut Scenes Test", "Start", "Quit");
		blIntermissionTest[INTERMISSION_TEST_START].setOnAction(e -> ui.startCutscenesTest());
		blIntermissionTest[INTERMISSION_TEST_QUIT].setOnAction(e -> ui.restartIntro());

		spGameLevel = addSpinner("Level", 1, 100, 1);
		spGameLevel.valueProperty().addListener((obs, oldVal, newVal) -> ui.enterLevel(newVal.intValue()));

		spGameCredit = addSpinner("Credit", 0, GameModel.MAX_CREDIT, GameController.it().credit());
		spGameCredit.valueProperty().addListener((obs, oldVal, newVal) -> GameController.it().setCredit(newVal.intValue()));

		cbAutopilot = addCheckBox("Autopilot", ui::toggleAutopilot);
		cbImmunity = addCheckBox("Player immune", ui::toggleImmunity);
	}

	@Override
	public void update() {
		super.update();

		comboGameVariant.setValue(game().variant());
		comboGameVariant.setDisable(GameController.it().state() != GameState.INTRO);

		comboInitialLives.setValue(game().getInitialLives());

		cbAutopilot.setSelected(GameController.it().isAutoControlled());
		cbImmunity.setSelected(GameController.it().isImmune());

		blGameLevel[GAME_LEVEL_START].setDisable(!(GameController.it().hasCredit() && game().level().isEmpty()));

		blGameLevel[GAME_LEVEL_QUIT].setDisable(game().level().isEmpty());

		blGameLevel[GAME_LEVEL_NEXT].setDisable(//
				!game().isPlaying() //
						|| (GameController.it().state() != GameState.HUNTING && GameController.it().state() != GameState.READY
								&& GameController.it().state() != GameState.CHANGING_TO_NEXT_LEVEL));

		blIntermissionTest[INTERMISSION_TEST_START].setDisable(//
				GameController.it().state() == GameState.INTERMISSION_TEST || GameController.it().state() != GameState.INTRO);

		blIntermissionTest[INTERMISSION_TEST_QUIT].setDisable(GameController.it().state() != GameState.INTERMISSION_TEST);

		game().level().ifPresent(level -> spGameLevel.getValueFactory().setValue(level.number()));
		if (!game().isPlaying() || GameController.it().state() == GameState.CHANGING_TO_NEXT_LEVEL) {
			spGameLevel.setDisable(true);
		} else {
			spGameLevel
					.setDisable(GameController.it().state() != GameState.READY && GameController.it().state() != GameState.HUNTING
							&& GameController.it().state() != GameState.CHANGING_TO_NEXT_LEVEL);
		}

		spGameCredit.getValueFactory().setValue(GameController.it().credit());
	}
}