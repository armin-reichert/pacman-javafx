/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.GlobalGameActions2D;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;

import static de.amr.games.pacman.lib.Globals.oneOf;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_AUTOPILOT;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_IMMUNITY;

/**
 * Game related settings.
 *
 * @author Armin Reichert
 */
public class InfoBoxGameControl extends InfoBox {

    private static final int GAME_LEVEL_START = 0;
    private static final int GAME_LEVEL_QUIT = 1;
    private static final int GAME_LEVEL_NEXT = 2;

    private static final int CUT_SCENES_TEST_START = 0;
    private static final int CUT_SCENES_TEST_QUIT = 1;

    private Spinner<Integer> spinnerCredit;
    private ComboBox<GameVariant> comboGameVariant;
    private ComboBox<Integer> comboInitialLives;
    private Button[] bgLevelActions;
    private Button[] bgCutScenesTest;
    private CheckBox cbAutopilot;
    private CheckBox cbImmunity;

    public void init(GameContext context) {
        super.init(context);

        spinnerCredit      = integerSpinner("Credit", 0, context.gameController().coinControl().maxCoins(), 0);
        comboGameVariant   = comboBox("Variant", GameVariant.values());
        comboInitialLives  = comboBox("Initial Lives", new Integer[] {3, 5});
        bgLevelActions     = buttonList("Game Level", "Start", "Quit", "Next");
        bgCutScenesTest    = buttonList("Cut Scenes Test", "Start", "Quit");
        cbAutopilot        = checkBox("Autopilot");
        cbImmunity         = checkBox("Pac-Man Immune");

        spinnerCredit.valueProperty().addListener((py, ov, number) -> context.gameController().coinControl().setNumCoins(number));

        comboGameVariant.setOnAction(e -> {
            if (comboGameVariant.getValue() != context.gameVariant()) {
                context.gameController().selectGame(comboGameVariant.getValue());
                context.gameController().restart(GameState.BOOT);
            }
        });

        setAction(bgCutScenesTest[CUT_SCENES_TEST_START], () -> GlobalGameActions2D.TEST_CUT_SCENES.execute(context));
        setAction(bgCutScenesTest[CUT_SCENES_TEST_QUIT],  () -> GlobalGameActions2D.RESTART_INTRO.execute(context));
        setAction(bgLevelActions[GAME_LEVEL_START],       () -> GlobalGameActions2D.START_GAME.execute(context));
        setAction(bgLevelActions[GAME_LEVEL_QUIT],        () -> GlobalGameActions2D.RESTART_INTRO.execute(context));
        setAction(bgLevelActions[GAME_LEVEL_NEXT],        () -> GlobalGameActions2D.CHEAT_NEXT_LEVEL.execute(context));
        setAction(comboInitialLives,                      () -> context.game().setInitialLives(comboInitialLives.getValue()));

        assignEditor(cbAutopilot, PY_AUTOPILOT);
        assignEditor(cbImmunity, PY_IMMUNITY);
    }

    @Override
    public void update() {
        super.update();

        GameModel game = context.game();
        GameState state = context.gameState();

        spinnerCredit.getValueFactory().setValue(context.gameController().coinControl().credit());
        comboGameVariant.setValue(game.variant());
        comboInitialLives.setValue(game.initialLives());

        spinnerCredit.setDisable(!(oneOf(state, GameState.INTRO, GameState.WAITING_FOR_START)));
        comboGameVariant.setDisable(state != GameState.INTRO);
        comboInitialLives.setDisable(state != GameState.INTRO);

        bgLevelActions[GAME_LEVEL_START].setDisable(isBooting() || !canStartLevel());
        bgLevelActions[GAME_LEVEL_QUIT].setDisable(isBooting() || context.game().levelNumber() == 0);
        bgLevelActions[GAME_LEVEL_NEXT].setDisable(isBooting() || !canEnterNextLevel());

        bgCutScenesTest[CUT_SCENES_TEST_START].setDisable(isBooting() || state != GameState.INTRO);
        bgCutScenesTest[CUT_SCENES_TEST_QUIT].setDisable(isBooting() || state != GameState.TESTING_CUT_SCENES);

        cbAutopilot.setDisable(isBooting());
        cbImmunity.setDisable(isBooting());
    }

    private boolean isBooting() {
        return context.gameState() == GameState.BOOT;
    }

    private boolean canStartLevel() {
        return context.game().canStartNewGame() && oneOf(context.gameState(), GameState.INTRO, GameState.WAITING_FOR_START);
    }

    private boolean canEnterNextLevel() {
        return context.game().isPlaying() && oneOf(context.gameState(), GameState.HUNTING);
    }
}