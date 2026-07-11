/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.test.CutScenesTestState;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.game.Game;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;

import java.util.List;

public class DS_GameControl extends GameDashboardSection {

    private static final int GAME_LEVEL_START = 0;
    private static final int GAME_LEVEL_QUIT = 1;
    private static final int GAME_LEVEL_NEXT = 2;

    private static final int CUT_SCENES_TEST_START = 0;
    private static final int CUT_SCENES_TEST_QUIT = 1;

    private Spinner<Integer> spinnerCredit;
    private ChoiceBox<Integer> choiceBoxInitialLives;
    private Button[] buttonGroupLevelActions;
    private Button[] buttonGroupCutScenesTest;
    private CheckBox cbCollisionCheckedTwice;

    public DS_GameControl() {
        super(DashboardID.GAME_CONTROL);
    }

    @Override
    public void connect(Game game) {
        final CoinMechanism coinMechanism = game.machine().coinMechanism();
        final CommonActions actions = game.actions();

        spinnerCredit            = intSpinner("Credit", 0, coinMechanism.maxCoins(), coinMechanism.numCoinsProperty());
        choiceBoxInitialLives    = choiceBox("Initial Lives", new Integer[] {3, 5});
        buttonGroupLevelActions  = buttonList("Game Level", List.of("Start", "Quit", "Next"));
        buttonGroupCutScenesTest = buttonList("Cut Scenes Test", List.of("Start", "Quit"));
        addDynamicInfo("Collision Mode", supplyGameRulesInfo(game, rules -> rules.getCollisionStrategy().name()));
        cbCollisionCheckedTwice  = checkBox("Collision Check 2x");

        setAction(choiceBoxInitialLives, () -> game.context().model().lives().setInitialCount(choiceBoxInitialLives.getValue()));

        //TODO Here we would need to access the Arcade-specific action to start the game
//        setGameAction(buttonGroupLevelActions[GAME_LEVEL_START],       actionToStartTheGamePlay);
        setGameAction(buttonGroupLevelActions[GAME_LEVEL_QUIT],        actions.gameFlowActions().actionRestartIntro());
        setGameAction(buttonGroupLevelActions[GAME_LEVEL_NEXT],        actions.cheatActions().actionEnterNextLevel());

        setGameAction(buttonGroupCutScenesTest[CUT_SCENES_TEST_START], actions.sceneTestActions().actionTestCutScenes());
        setGameAction(buttonGroupCutScenesTest[CUT_SCENES_TEST_QUIT],  actions.gameFlowActions().actionRestartIntro());

        cbCollisionCheckedTwice.setOnAction(_ ->
            game.context().model().rules().collisionDoubleCheckedProperty().set(cbCollisionCheckedTwice.isSelected()));
    }

    @Override
    public void update(Game game) {
        super.update(game);

        final GameContext context = game.context();
        final GameModel model = context.model();
        final GameState state = context.state();

        choiceBoxInitialLives.setValue(model.lives().initialCount());
        choiceBoxInitialLives.setDisable(!GameStateID.GAME_INTRO.identifies(state));

        final boolean creditDisabled = !state.isOneOf(GameStateID.GAME_INTRO, GameStateID.GAME_PREPARATION);
        spinnerCredit.setDisable(creditDisabled);

        final boolean booting = GameStateID.BOOT.identifies(state);
        buttonGroupLevelActions[GAME_LEVEL_START].setDisable(booting || !canStartLevel(game, state));
        buttonGroupLevelActions[GAME_LEVEL_NEXT] .setDisable(booting || !canEnterNextLevel(model, state));
        buttonGroupLevelActions[GAME_LEVEL_QUIT] .setDisable(booting || model.optLevel().isEmpty());

        buttonGroupCutScenesTest[CUT_SCENES_TEST_START].setDisable(booting || !GameStateID.GAME_INTRO.identifies(state));
        buttonGroupCutScenesTest[CUT_SCENES_TEST_QUIT].setDisable(booting || !(state instanceof CutScenesTestState));

        cbCollisionCheckedTwice.setSelected(context.model().rules().collisionDoubleCheckedProperty().get());
    }

    private boolean canStartLevel(Game game, GameState gameState) {
        boolean isArcadeGame = GameVariantID.isArcadeGameName(game.variants().currentVariantName());
        if (!isArcadeGame) return true; //TODO not 100% correct but we cannot access Tengen game model from here
        return !game.context().coinMechanism().isEmpty()
            && gameState.isOneOf(GameStateID.GAME_INTRO, GameStateID.GAME_PREPARATION);
    }

    private boolean canEnterNextLevel(GameModel game, GameState gameState) {
        return game.isPlaying() && GameStateID.GAME_LEVEL_PLAYING.identifies(gameState);
    }
}