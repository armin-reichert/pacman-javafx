/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.GameSceneContext;
import de.amr.games.pacman.ui.fx.PacManGames2dUI;
import de.amr.games.pacman.ui.fx.util.Theme;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;

import static de.amr.games.pacman.ui.fx.PacManGames2dUI.PY_USE_ALTERNATE_MAPS;
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
    private final Button[] buttonsIntermissionTest;
    private final CheckBox cbAutopilot;
    private final CheckBox cbImmunity;
    private final CheckBox cbUseRandomMaps;

    public InfoBoxGameControl(Theme theme, String title) {
        super(theme, title);

        spinnerCredit = addSpinner("Credit", 0, GameController.MAX_CREDIT, 0);
        comboGameVariant = addComboBox("Variant", GameVariant.values());
        comboInitialLives = addComboBox("Initial Lives", new Integer[]{3, 5});
        buttonsLevelActions = addButtonList("Game Level", "Start", "Quit", "Next");
        buttonsIntermissionTest = addButtonList("Cut Scenes Test", "Start", "Quit");
        cbAutopilot = addCheckBox("Autopilot");
        cbImmunity = addCheckBox("Pac-Man Immune");
        cbUseRandomMaps = addCheckBox("Use Map Variations");
    }

    @Override
    public void init(GameSceneContext context) {
        super.init(context);
        comboGameVariant.setOnAction(e -> {
            var selectedVariant = comboGameVariant.getValue();
            if (selectedVariant != context.game()) {
                context.gameController().selectGame(selectedVariant);
                context.gameController().restart(GameState.BOOT);
            }
        });
        buttonsIntermissionTest[INTERMISSION_TEST_START].setOnAction(e -> actionHandler().startCutscenesTest());
        buttonsIntermissionTest[INTERMISSION_TEST_QUIT].setOnAction(e -> actionHandler().restartIntro());
        comboInitialLives.setOnAction(e -> context.game().setInitialLives(comboInitialLives.getValue()));
        buttonsLevelActions[GAME_LEVEL_START].setOnAction(e -> actionHandler().startGame());
        buttonsLevelActions[GAME_LEVEL_QUIT].setOnAction(e -> actionHandler().restartIntro());
        buttonsLevelActions[GAME_LEVEL_NEXT].setOnAction(e -> context.actionHandler().cheatEnterNextLevel());
        spinnerCredit.valueProperty().addListener((py, ov, nv) -> context.gameController().setCredit(nv));
        spinnerCredit.getValueFactory().setValue(context.gameController().credit());
        cbAutopilot.setOnAction(e -> actionHandler().toggleAutopilot());
        cbImmunity.setOnAction(e -> actionHandler().toggleImmunity());
        cbUseRandomMaps.setOnAction(e -> PY_USE_ALTERNATE_MAPS.set(cbUseRandomMaps.isSelected()));
    }

    @Override
    public void update() {
        super.update();

        comboGameVariant.setValue(context.game());
        comboGameVariant.setDisable(context.gameState() != GameState.INTRO);
        comboInitialLives.setValue(context.game().initialLives());
        cbAutopilot.setSelected(PY_USE_AUTOPILOT.get());
        cbImmunity.setSelected(context.gameController().isPacImmune());
        cbUseRandomMaps.setSelected(PY_USE_ALTERNATE_MAPS.get());
        buttonsLevelActions[GAME_LEVEL_START].setDisable(!canStartLevel());
        buttonsLevelActions[GAME_LEVEL_QUIT].setDisable(context.game().level().isEmpty());
        buttonsLevelActions[GAME_LEVEL_NEXT].setDisable(!canEnterNextLevel());
        buttonsIntermissionTest[INTERMISSION_TEST_START].setDisable(
            context.gameState() == GameState.INTERMISSION_TEST || context.gameState() != GameState.INTRO);
        buttonsIntermissionTest[INTERMISSION_TEST_QUIT].setDisable(context.gameState() != GameState.INTERMISSION_TEST);
        spinnerCredit.getValueFactory().setValue(context.gameController().credit());
    }

    private boolean canStartLevel() {
        return context.gameController().hasCredit()
            && Globals.oneOf(context.gameState(), GameState.INTRO, GameState.CREDIT);
    }

    private boolean canEnterNextLevel() {
        return context.game().isPlaying()
            && Globals.oneOf(context.gameState(), GameState.HUNTING);
    }
}