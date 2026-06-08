/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.subviews.dashboard;

import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.ui.action.TestActions;
import de.amr.pacmanfx.ui.game.Game;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;

import java.util.List;

import static de.amr.pacmanfx.ui.action.CheatActions.ACTION_ENTER_NEXT_LEVEL;
import static de.amr.pacmanfx.ui.action.CommonActions.ACTION_RESTART_INTRO;

public class DashboardSectionGameControl extends DashboardSection {

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

    public DashboardSectionGameControl(Dashboard dashboard) {
        super(dashboard);
    }

    @Override
    public void connect(Game context) {
        final CoinMechanism coinMechanism = context.coinMechanism();
        spinnerCredit            = addIntSpinner("Credit", 0, coinMechanism.maxCoins(), coinMechanism.numCoinsProperty());
        choiceBoxInitialLives    = addChoiceBox("Initial Lives", new Integer[] {3, 5});
        buttonGroupLevelActions  = addButtonList("Game Level", List.of("Start", "Quit", "Next"));
        buttonGroupCutScenesTest = addButtonList("Cut Scenes Test", List.of("Start", "Quit"));
        addDynamicLabeledValue("Collision Mode", context.currentGameContext()::collisionStrategy);
        cbCollisionCheckedTwice  = addCheckBox("Collision Check 2x");

        setAction(choiceBoxInitialLives, () -> context.currentGameContext().model().lives().setInitialCount(choiceBoxInitialLives.getValue()));

        //setAction(buttonGroupLevelActions[GAME_LEVEL_START], ArcadeActions.ACTION_START_GAME); //TODO FIXME!
        setAction(context, buttonGroupLevelActions[GAME_LEVEL_QUIT], ACTION_RESTART_INTRO);
        setAction(context, buttonGroupLevelActions[GAME_LEVEL_NEXT], ACTION_ENTER_NEXT_LEVEL);

        setAction(context, buttonGroupCutScenesTest[CUT_SCENES_TEST_START], TestActions.ACTION_CUT_SCENES_TEST);
        setAction(context, buttonGroupCutScenesTest[CUT_SCENES_TEST_QUIT], ACTION_RESTART_INTRO);

        cbCollisionCheckedTwice.setOnAction(_ -> context.setCollisionDoubleChecked(cbCollisionCheckedTwice.isSelected()));
    }

    @Override
    public void update() {
        super.update();

        if (dashboard.game() != null) {
            final GameContext gameContext = dashboard.game().currentGameContext();
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

            cbCollisionCheckedTwice.setSelected(gameContext.isCollisionDoubleChecked());
        }
    }

    private boolean canStartLevel(GameContext gameContext, GameModel game, GameState gameState) {
        return game.canStartNewGame(gameContext)
            && gameState.isOneOf(GameStateID.GAME_INTRO, GameStateID.GAME_PREPARATION);
    }

    private boolean canEnterNextLevel(GameModel game, GameState gameState) {
        return game.isPlaying() && GameStateID.GAME_LEVEL_PLAYING.identifies(gameState);
    }
}