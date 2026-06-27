/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.ui.game.Game;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;

import java.util.List;

public class DS_GameControl extends DashboardSection {

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

    public DS_GameControl(Dashboard dashboard) {
        super(dashboard);
    }

    @Override
    public void connect(Game game) {
        final CoinMechanism coinMechanism = game.coinMechanism();
        spinnerCredit            = addIntSpinner("Credit", 0, coinMechanism.maxCoins(), coinMechanism.numCoinsProperty());
        choiceBoxInitialLives    = addChoiceBox("Initial Lives", new Integer[] {3, 5});
        buttonGroupLevelActions  = addButtonList("Game Level", List.of("Start", "Quit", "Next"));
        buttonGroupCutScenesTest = addButtonList("Cut Scenes Test", List.of("Start", "Quit"));
        addDynamicInfo("Collision Mode", () -> game.currentGameContext().rules().collisionStrategyProperty().get());
        cbCollisionCheckedTwice  = addCheckBox("Collision Check 2x");

        setAction(choiceBoxInitialLives, () -> game.currentGameContext().model().lives().setInitialCount(choiceBoxInitialLives.getValue()));

        setAction(game, buttonGroupLevelActions[GAME_LEVEL_QUIT],
            game.actions().gameFlowActions().actionRestartIntro());

        setAction(game, buttonGroupLevelActions[GAME_LEVEL_NEXT],
            game.actions().cheatActions().actionEnterNextLevel());

        setAction(game, buttonGroupCutScenesTest[CUT_SCENES_TEST_START],
            game.actions().sceneTestActions().actionTestCutScenes());

        setAction(game, buttonGroupCutScenesTest[CUT_SCENES_TEST_QUIT], game.actions().gameFlowActions().actionRestartIntro());

        cbCollisionCheckedTwice.setOnAction(_ ->
            game.currentGameContext().rules().collisionDoubleCheckedProperty().set(cbCollisionCheckedTwice.isSelected()));
    }

    @Override
    public void update(Game game) {
        super.update(game);

        final GameContext gameContext = game.currentGameContext();
        final GameModel gameModel = gameContext.model();
        final GameState gameState = gameContext.state();

        choiceBoxInitialLives.setValue(gameModel.lives().initialCount());
        choiceBoxInitialLives.setDisable(!GameStateID.GAME_INTRO.identifies(gameState));

        final boolean creditDisabled = !gameState.isOneOf(GameStateID.GAME_INTRO, GameStateID.GAME_PREPARATION);
        spinnerCredit.setDisable(creditDisabled);

        final boolean booting = GameStateID.BOOT.identifies(gameState);
        buttonGroupLevelActions[GAME_LEVEL_START].setDisable(booting || !canStartLevel(gameContext, gameModel, gameState));
        buttonGroupLevelActions[GAME_LEVEL_NEXT] .setDisable(booting || !canEnterNextLevel(gameModel, gameState));
        buttonGroupLevelActions[GAME_LEVEL_QUIT] .setDisable(booting || gameModel.optGameLevel().isEmpty());

        buttonGroupCutScenesTest[CUT_SCENES_TEST_START].setDisable(booting || !GameStateID.GAME_INTRO.identifies(gameState));
        buttonGroupCutScenesTest[CUT_SCENES_TEST_QUIT].setDisable(booting || !(gameState instanceof CutScenesTestState));

        cbCollisionCheckedTwice.setSelected(gameContext.rules().collisionDoubleCheckedProperty().get());
    }

    private boolean canStartLevel(GameContext gameContext, GameModel game, GameState gameState) {
        return game.canStartNewGame(gameContext)
            && gameState.isOneOf(GameStateID.GAME_INTRO, GameStateID.GAME_PREPARATION);
    }

    private boolean canEnterNextLevel(GameModel game, GameState gameState) {
        return game.isPlaying() && GameStateID.GAME_LEVEL_PLAYING.identifies(gameState);
    }
}