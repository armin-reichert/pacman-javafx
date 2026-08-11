/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.test.CutScenesTestState;
import de.amr.pacmanfx.core.session.GameSession;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
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
    public void setGameAppContext(GameAppContext appContext) {
        final CoinMechanism coinMechanism = appContext.currentGameContext().coinMechanism();
        final CommonGameActions actions = appContext.commonActions();

        spinnerCredit            = intSpinner("Credit", 0, coinMechanism.maxCoins(), coinMechanism.numCoinsProperty());
        choiceBoxInitialLives    = choiceBox("Initial Lives", new Integer[] {3, 5});
        buttonGroupLevelActions  = buttonList("Game Level", List.of("Start", "Quit", "Next"));
        buttonGroupCutScenesTest = buttonList("Cut Scenes Test", List.of("Start", "Quit"));
        addDynamicInfo("Collision Mode", fnGameRulesInfo(appContext, rules -> rules.actorCollisionRules().getCollisionStrategy().name()));
        cbCollisionCheckedTwice  = checkBox("Collision Check 2x");

        setAction(choiceBoxInitialLives, () -> appContext.currentGameContext().model().setInitialLifeCount(choiceBoxInitialLives.getValue()));

        //TODO Here we would need to access the Arcade-specific action to start the game
//        setGameAction(buttonGroupLevelActions[GAME_LEVEL_START],       actionToStartTheGamePlay);
        setGameAction(buttonGroupLevelActions[GAME_LEVEL_QUIT],        actions.gameFlowActions().actionRestartIntro());
        setGameAction(buttonGroupLevelActions[GAME_LEVEL_NEXT],        actions.cheatActions().actionEnterNextLevel());

        setGameAction(buttonGroupCutScenesTest[CUT_SCENES_TEST_START], actions.sceneTestActions().actionTestCutScenes());
        setGameAction(buttonGroupCutScenesTest[CUT_SCENES_TEST_QUIT],  actions.gameFlowActions().actionRestartIntro());

        cbCollisionCheckedTwice.setOnAction(_ ->
            appContext.currentGameContext().model().rules().actorCollisionRules().collisionDoubleCheckedProperty()
                .set(cbCollisionCheckedTwice.isSelected()));
    }

    @Override
    public void update(GameAppContext appContext) {
        super.update(appContext);

        final GameContext gameContext = appContext.currentGameContext();
        final GameSession session = gameContext.session();
        final GameModel model = gameContext.model();
        final GameState state = gameContext.state();

        choiceBoxInitialLives.setValue(model.initialLifeCount());
        choiceBoxInitialLives.setDisable(!CommonGameStateID.GAME_INTRO.hasSameNameAs(state));

        final boolean creditDisabled = !state.nameIsOneOf(CommonGameStateID.GAME_INTRO, CommonGameStateID.GAME_PREPARATION);
        spinnerCredit.setDisable(creditDisabled);

        final boolean booting = CommonGameStateID.BOOT.hasSameNameAs(state);
        buttonGroupLevelActions[GAME_LEVEL_START].setDisable(booting || !canStartLevel(appContext, state));
        buttonGroupLevelActions[GAME_LEVEL_NEXT] .setDisable(booting || !canEnterNextLevel(gameContext.session(), state));
        buttonGroupLevelActions[GAME_LEVEL_QUIT] .setDisable(booting || session.optLevel().isEmpty());

        buttonGroupCutScenesTest[CUT_SCENES_TEST_START].setDisable(booting || !CommonGameStateID.GAME_INTRO.hasSameNameAs(state));
        buttonGroupCutScenesTest[CUT_SCENES_TEST_QUIT].setDisable(booting || !(state instanceof CutScenesTestState));

        cbCollisionCheckedTwice.setSelected(gameContext.model().rules().actorCollisionRules().isCollisionDoubleChecked());
    }

    private boolean canStartLevel(GameAppContext appContext, GameState gameState) {
        boolean isArcadeGame = GameVariantID.isArcadeGameName(appContext.variants().currentVariantName());
        if (!isArcadeGame) return true; //TODO not 100% correct but we cannot access Tengen game model from here
        return !appContext.currentGameContext().coinMechanism().isEmpty()
            && gameState.nameIsOneOf(CommonGameStateID.GAME_INTRO, CommonGameStateID.GAME_PREPARATION);
    }

    private boolean canEnterNextLevel(GameSession session, GameState gameState) {
        return session.isPlaying() && CommonGameStateID.GAME_LEVEL_PLAYING.hasSameNameAs(gameState);
    }
}