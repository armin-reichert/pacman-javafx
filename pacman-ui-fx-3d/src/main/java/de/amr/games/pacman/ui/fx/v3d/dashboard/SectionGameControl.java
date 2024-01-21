/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.v3d.ActionHandler3D;
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

	public SectionGameControl(Theme theme, String title) {
		super(theme, title, Dashboard.MIN_LABEL_WIDTH, Dashboard.TEXT_COLOR, Dashboard.TEXT_FONT, Dashboard.LABEL_FONT);

		comboGameVariant = addComboBox("Variant", GameVariant.MS_PACMAN, GameVariant.PACMAN);
		comboGameVariant.setOnAction(e -> {
			var selectedVariant = comboGameVariant.getValue();
			if (selectedVariant != game().variant()) {
				sceneContext.gameController().startNewGame(selectedVariant);
			}
		});

		comboInitialLives = addComboBox("Initial Lives", 3, 5);
		comboInitialLives.setOnAction(e -> game().setInitialLives((comboInitialLives.getValue().shortValue())));

		blGameLevel = addButtonList("Game Level", "Start", "Quit", "Next");
		blIntermissionTest = addButtonList("Cut Scenes Test", "Start", "Quit");
		spGameLevel = addSpinner("Level", 1, 100, 1);
		spGameCredit = addSpinner("Credit", 0, GameModel.MAX_CREDIT, sceneContext.gameController().credit());
		spGameCredit.valueProperty().addListener((py, ov, nv) -> sceneContext.gameController().setCredit(nv.intValue()));
		cbAutopilot = addCheckBox("Autopilot");
		cbImmunity = addCheckBox("Player immune");
	}

	@Override
	public void init(GameSceneContext sceneContext, ActionHandler3D actionHandler) {
		super.init(sceneContext, actionHandler);

		blIntermissionTest[INTERMISSION_TEST_START].setOnAction(e -> actionHandler.startCutscenesTest());
		blIntermissionTest[INTERMISSION_TEST_QUIT].setOnAction(e -> actionHandler.restartIntro());
		blGameLevel[GAME_LEVEL_START].setOnAction(e -> sceneContext.gameController().startPlaying());
		blGameLevel[GAME_LEVEL_QUIT].setOnAction(e -> actionHandler.restartIntro());
		blGameLevel[GAME_LEVEL_NEXT].setOnAction(e -> sceneContext.gameController().cheatEnterNextLevel());
		spGameLevel.valueProperty().addListener((py, ov, nv) -> actionHandler.enterLevel(nv.intValue()));
		cbAutopilot.setOnAction(e -> actionHandler.toggleAutopilot());
		cbImmunity.setOnAction(e -> actionHandler.toggleImmunity());
	}

	@Override
	public void update() {
		super.update();

		comboGameVariant.setValue(game().variant());
		comboGameVariant.setDisable(sceneContext.gameState() != GameState.INTRO);
		comboInitialLives.setValue((int) game().initialLives());
		cbAutopilot.setSelected(sceneContext.gameController().isAutoControlled());
		cbImmunity.setSelected(sceneContext.gameController().isImmune());
		blGameLevel[GAME_LEVEL_START].setDisable(!(sceneContext.gameController().hasCredit() && game().level().isEmpty()));
		blGameLevel[GAME_LEVEL_QUIT].setDisable(game().level().isEmpty());
		blGameLevel[GAME_LEVEL_NEXT].setDisable(!game().isPlaying() ||
			(  sceneContext.gameState() != GameState.HUNTING
			&& sceneContext.gameState() != GameState.READY
			&& sceneContext.gameState() != GameState.CHANGING_TO_NEXT_LEVEL));
		blIntermissionTest[INTERMISSION_TEST_START].setDisable(
			sceneContext.gameState() == GameState.INTERMISSION_TEST || sceneContext.gameState() != GameState.INTRO);
		blIntermissionTest[INTERMISSION_TEST_QUIT].setDisable(sceneContext.gameState() != GameState.INTERMISSION_TEST);
		game().level().ifPresent(level -> spGameLevel.getValueFactory().setValue(level.number()));
		if (!game().isPlaying() || sceneContext.gameState() == GameState.CHANGING_TO_NEXT_LEVEL) {
			spGameLevel.setDisable(true);
		} else {
			spGameLevel.setDisable(sceneContext.gameState() != GameState.READY
				&& sceneContext.gameState() != GameState.HUNTING
				&& sceneContext.gameState() != GameState.CHANGING_TO_NEXT_LEVEL);
		}
		spGameCredit.getValueFactory().setValue(sceneContext.gameController().credit());
	}
}