/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariants;
import de.amr.games.pacman.ui.fx.GameSceneContext;
import de.amr.games.pacman.ui.fx.util.Theme;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;

import static de.amr.games.pacman.ui.fx.PacManGames2dUI.PY_USE_AUTOPILOT;

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

    private final Spinner<Integer> spinnerCredit;
    private final ComboBox<GameModel> comboGameVariant;
    private final ComboBox<Integer> comboInitialLives;
    private final Button[] buttonsLevelActions;
    private final Spinner<Integer> spinnerLevelNumber;
    private final Button[] buttonsIntermissionTest;
    private final CheckBox cbAutopilot;
    private final CheckBox cbImmunity;

    public InfoBoxGameControl(Theme theme, String title) {
        super(theme, title);

        spinnerCredit = addSpinner("Credit", 0, GameController.MAX_CREDIT, 0);
        comboGameVariant = addComboBox("Variant", GameVariants.values());
        comboInitialLives = addComboBox("Initial Lives", new Integer[]{3, 5});
        buttonsLevelActions = addButtonList("Game Level", "Start", "Quit", "Next");
        spinnerLevelNumber = addSpinner("Level Number", 1, 100, 1);
        buttonsIntermissionTest = addButtonList("Cut Scenes Test", "Start", "Quit");
        cbAutopilot = addCheckBox("Autopilot");
        cbImmunity = addCheckBox("Pac-Man Immune");
    }

    @Override
    public void init(GameSceneContext sceneContext) {
        super.init(sceneContext);
        comboGameVariant.setOnAction(e -> {
            var selectedVariant = comboGameVariant.getValue();
            if (selectedVariant != sceneContext.game()) {
                sceneContext.gameController().selectGame(selectedVariant);
                sceneContext.gameController().restart(GameState.BOOT);
            }
        });
        buttonsIntermissionTest[INTERMISSION_TEST_START].setOnAction(e -> actionHandler().startCutscenesTest());
        buttonsIntermissionTest[INTERMISSION_TEST_QUIT].setOnAction(e -> actionHandler().restartIntro());
        comboInitialLives.setOnAction(e -> sceneContext.game().setInitialLives((comboInitialLives.getValue().shortValue())));
        buttonsLevelActions[GAME_LEVEL_START].setOnAction(e -> actionHandler().startGame());
        buttonsLevelActions[GAME_LEVEL_QUIT].setOnAction(e -> actionHandler().restartIntro());
        buttonsLevelActions[GAME_LEVEL_NEXT].setOnAction(e -> sceneContext.actionHandler().cheatEnterNextLevel());
        spinnerLevelNumber.valueProperty().addListener((py, ov, nv) -> actionHandler().enterLevel(nv));
        spinnerLevelNumber.getValueFactory().setValue(sceneContext.game().level() != null
            ? sceneContext.game().level().levelNumber() : 1);
        spinnerCredit.valueProperty().addListener((py, ov, nv) -> sceneContext.gameController().setCredit(nv));
        spinnerCredit.getValueFactory().setValue(sceneContext.gameController().credit());
        cbAutopilot.setOnAction(e -> actionHandler().toggleAutopilot());
        cbImmunity.setOnAction(e -> actionHandler().toggleImmunity());
    }

    @Override
    public void update() {
        super.update();

        comboGameVariant.setValue(sceneContext.game());
        comboGameVariant.setDisable(sceneContext.gameState() != GameState.INTRO);
        comboInitialLives.setValue((int) sceneContext.game().initialLives());
        cbAutopilot.setSelected(PY_USE_AUTOPILOT.get());
        cbImmunity.setSelected(sceneContext.gameController().isPacImmune());
        buttonsLevelActions[GAME_LEVEL_START].setDisable(!canStartLevel());
        buttonsLevelActions[GAME_LEVEL_QUIT].setDisable(sceneContext.game().level() == null);
        buttonsLevelActions[GAME_LEVEL_NEXT].setDisable(!canEnterNextLevel());
        buttonsIntermissionTest[INTERMISSION_TEST_START].setDisable(
            sceneContext.gameState() == GameState.INTERMISSION_TEST || sceneContext.gameState() != GameState.INTRO);
        buttonsIntermissionTest[INTERMISSION_TEST_QUIT].setDisable(sceneContext.gameState() != GameState.INTERMISSION_TEST);
        if (sceneContext.game().level() != null) {
            spinnerLevelNumber.getValueFactory().setValue(sceneContext.game().level().levelNumber());
        }
        if (!sceneContext.game().isPlaying() || sceneContext.gameState() == GameState.LEVEL_TRANSITION) {
            spinnerLevelNumber.setDisable(true);
        } else {
            spinnerLevelNumber.setDisable(sceneContext.gameState() != GameState.READY
                && sceneContext.gameState() != GameState.HUNTING
                && sceneContext.gameState() != GameState.LEVEL_TRANSITION);
        }
        spinnerCredit.getValueFactory().setValue(sceneContext.gameController().credit());
    }

    private boolean canStartLevel() {
        return sceneContext.gameController().hasCredit()
            && Globals.oneOf(sceneContext.gameState(), GameState.INTRO, GameState.CREDIT);
    }

    private boolean canEnterNextLevel() {
        return sceneContext.game().isPlaying()
            && Globals.oneOf(sceneContext.gameState(), GameState.HUNTING);
    }
}