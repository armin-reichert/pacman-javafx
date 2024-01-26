/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import de.amr.games.pacman.ui.fx.util.Theme;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;

/**
 * Game related settings.
 * 
 * @author Armin Reichert
 */
public class InfoBoxGameControl extends InfoBox {

	private static final int GAME_LEVEL_START = 0;
	private static final int GAME_LEVEL_QUIT = 1;
	private static final int GAME_LEVEL_NEXT = 2;

	private static final int INTERMISSION_TEST_START = 0;
	private static final int INTERMISSION_TEST_QUIT = 1;

	private final ComboBox<GameVariant> comboGameVariant;
	private final ComboBox<Integer> comboInitialLives;
	private final Button[] buttonsGameLevel;
	private final Button[] buttonsIntermissionTest;
	private final Spinner<Integer> spinnerGameLevel;
	private final Spinner<Integer> spinnerGameCredit;
	private final CheckBox cbAutopilot;
	private final CheckBox cbImmunity;

	public InfoBoxGameControl(Theme theme, String title) {
		super(theme, title);

		comboGameVariant = addComboBox("Variant", GameVariant.MS_PACMAN, GameVariant.PACMAN);
		comboInitialLives = addComboBox("Initial Lives", 3, 5);
		buttonsGameLevel = addButtonList("Game Level", "Start", "Quit", "Next");
		buttonsIntermissionTest = addButtonList("Cut Scenes Test", "Start", "Quit");
		spinnerGameLevel = addSpinner("Level", 1, 100, 1);
		spinnerGameCredit = addSpinner("Credit", 0, GameModel.MAX_CREDIT, 0);
		cbAutopilot = addCheckBox("Autopilot");
		cbImmunity = addCheckBox("Player immune");
	}

	@Override
	public void init(GameSceneContext sceneContext) {
		super.init(sceneContext);

		comboGameVariant.setOnAction(e -> {
			var selectedVariant = comboGameVariant.getValue();
			if (selectedVariant != sceneContext.game().variant()) {
				sceneContext.gameController().startNewGame(selectedVariant);
			}
		});
		buttonsIntermissionTest[INTERMISSION_TEST_START].setOnAction(e -> actionHandler().startCutscenesTest());
		buttonsIntermissionTest[INTERMISSION_TEST_QUIT].setOnAction(e -> actionHandler().restartIntro());
		comboInitialLives.setOnAction(e -> sceneContext.game().setInitialLives((comboInitialLives.getValue().shortValue())));
		buttonsGameLevel[GAME_LEVEL_START].setOnAction(e -> sceneContext.gameController().startPlaying());
		buttonsGameLevel[GAME_LEVEL_QUIT].setOnAction(e -> actionHandler().restartIntro());
		buttonsGameLevel[GAME_LEVEL_NEXT].setOnAction(e -> sceneContext.gameController().cheatEnterNextLevel());
		spinnerGameLevel.valueProperty().addListener((py, ov, nv) -> actionHandler().enterLevel(nv));
		spinnerGameLevel.getValueFactory().setValue(sceneContext.gameLevel().isPresent() ? sceneContext.gameLevel().get().number() : 1);
		spinnerGameCredit.valueProperty().addListener((py, ov, nv) -> sceneContext.gameController().setCredit(nv));
		spinnerGameCredit.getValueFactory().setValue(sceneContext.gameController().credit());
		cbAutopilot.setOnAction(e -> actionHandler().toggleAutopilot());
		cbImmunity.setOnAction(e -> actionHandler().toggleImmunity());
	}

	@Override
	public void update() {
		super.update();

		comboGameVariant.setValue(sceneContext.game().variant());
		comboGameVariant.setDisable(sceneContext.gameState() != GameState.INTRO);
		comboInitialLives.setValue((int) sceneContext.game().initialLives());
		cbAutopilot.setSelected(sceneContext.gameController().isAutoControlled());
		cbImmunity.setSelected(sceneContext.gameController().isImmune());
		buttonsGameLevel[GAME_LEVEL_START].setDisable(!(sceneContext.gameController().hasCredit() && sceneContext.game().level().isEmpty()));
		buttonsGameLevel[GAME_LEVEL_QUIT].setDisable(sceneContext.game().level().isEmpty());
		buttonsGameLevel[GAME_LEVEL_NEXT].setDisable(!sceneContext.game().isPlaying() ||
			(  sceneContext.gameState() != GameState.HUNTING
			&& sceneContext.gameState() != GameState.READY
			&& sceneContext.gameState() != GameState.CHANGING_TO_NEXT_LEVEL));
		buttonsIntermissionTest[INTERMISSION_TEST_START].setDisable(
			sceneContext.gameState() == GameState.INTERMISSION_TEST || sceneContext.gameState() != GameState.INTRO);
		buttonsIntermissionTest[INTERMISSION_TEST_QUIT].setDisable(sceneContext.gameState() != GameState.INTERMISSION_TEST);
		sceneContext.game().level().ifPresent(level -> spinnerGameLevel.getValueFactory().setValue(level.number()));
		if (!sceneContext.game().isPlaying() || sceneContext.gameState() == GameState.CHANGING_TO_NEXT_LEVEL) {
			spinnerGameLevel.setDisable(true);
		} else {
			spinnerGameLevel.setDisable(sceneContext.gameState() != GameState.READY
				&& sceneContext.gameState() != GameState.HUNTING
				&& sceneContext.gameState() != GameState.CHANGING_TO_NEXT_LEVEL);
		}
		spinnerGameCredit.getValueFactory().setValue(sceneContext.gameController().credit());
	}
}