/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacManSoundID;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameExtension;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;
import de.amr.pacmanfx.tengenmspacman.model.BoosterMode;
import de.amr.pacmanfx.tengenmspacman.model.Difficulty;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.input.JoypadButton;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.KeyCode;
import org.tinylog.Logger;

import java.io.IOException;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantUIConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantUIConfig.NES_SCREEN_WIDTH;
import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.combine;

/**
 * Options scene for Ms. Pac-Man Tengen.
 *
 * <p></p>The high-score is cleared if player type (1 player, 2 players etc.), map category or difficulty are
 * changed.
 *
 * @see <a href="https://github.com/RussianManSMWC/Ms.-Pac-Man-NES-Tengen-Disassembly/blob/main/MsPacManTENGENDis.asm:9545">Disassembly</a>.
 */
public class TengenMsPacMan_OptionsScene extends AbstractGameScene2D {

    public static final byte OPTION_PLAYERS = 0;
    public static final byte OPTION_PAC_BOOSTER = 1;
    public static final byte OPTION_DIFFICULTY = 2;
    public static final byte OPTION_MAZE_SELECTION = 3;
    public static final byte OPTION_STARTING_LEVEL = 4;

    public static final byte NUM_OPTIONS = 5;

    private static final byte MIN_START_LEVEL = 1;
    private static final byte MAX_START_LEVEL = 32;

    private static final int INITIAL_DELAY = 20; //TODO verify
    private static final int IDLE_TIMEOUT = 1530; // 25,5 sec TODO verify

    private final IntegerProperty selectedOption = new SimpleIntegerProperty() {
        @Override
        protected void invalidated() {
            app().ui().sounds().play(TengenMsPacManSoundID.OPTION_SELECTION_CHANGE);
            idleTicks = 0;
        }
    };

    private int idleTicks;
    public int initialDelay;

    public TengenMsPacMan_OptionsScene(GameAppContext appContext) {
        super(appContext);
        unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        unscaledHeightProperty().set(NES_SCREEN_HEIGHT);
    }

    @Override
    public void onActivate() {
        final TengenMsPacMan_GamePlay gamePlay = (TengenMsPacMan_GamePlay) game().variant().gamePlay();
        final GameSession session = game().session();
        session.hud().hide();

        final var actions = app().currentGameVariantUIConfig().getExtensionValue(
            TengenMsPacMan_GameExtension.ACTIONS, TengenMsPacMan_Actions.class);

        actionBindings().selectAnyMatchingBinding(actions.actionStartPlaying(), actions.localBindings());
        actionBindings().selectAnyMatchingBinding(actions.actionToggleJoypadBindingsDisplayed(), actions.localBindings());
        actionBindings().bindActionToKeyCombination(actions.actionSelectNextJoypadKeyBinding(), combine().alt().key(KeyCode.J));
        actionBindings().registerAllBindings(app().commonActions().sceneTestActions().bindings());

        selectedOption.set(OPTION_PAC_BOOSTER);
        gamePlay.setCanStartNewGame(session, true);

        idleTicks = 0;
        initialDelay = INITIAL_DELAY;
    }

    @Override
    public void onTick(GameContext game) {
        if (initialDelay > 0) {
            --initialDelay;
            return;
        }
        if (idleTicks < IDLE_TIMEOUT) {
            idleTicks += 1;
        } else {
            gameFlow().enterState(game(), CommonGameStateID.GAME_INTRO);
        }
    }

    private void optionValueChanged() {
        app().ui().sounds().play(TengenMsPacManSoundID.OPTION_VALUE_CHANGE);
        idleTicks = 0;
    }

    public int selectedOption() {
        return selectedOption.get();
    }

    @Override
    public void onInput() {
        final GameSession session = game().session();

        if (app().input().joypad().isButtonPressed(JoypadButton.DOWN)) {
            selectedOption.set(selectedOption() + 1 < NUM_OPTIONS ? selectedOption() + 1 : 0);
        }
        else if (app().input().joypad().isButtonPressed(JoypadButton.UP)) {
            selectedOption.set(selectedOption() == 0 ? NUM_OPTIONS - 1 : selectedOption() - 1);
        }
        // Button "A" on the joypad is located right of "B": select next value
        else if (app().input().joypad().isButtonPressed(JoypadButton.A) || app().input().keyboard().isKeyPressed(KeyCode.RIGHT)) {
            switch (selectedOption()) {
                case OPTION_PAC_BOOSTER    -> setNextPacBoosterValue(session);
                case OPTION_DIFFICULTY     -> setNextDifficultyValue(session);
                case OPTION_MAZE_SELECTION -> setNextMapCategoryValue(session);
                case OPTION_STARTING_LEVEL -> setNextStartLevelValue();
            }
        }
        // Button "B" is left of "A": select previous value
        else if (app().input().joypad().isButtonPressed(JoypadButton.B) || app().input().keyboard().isKeyPressed(KeyCode.LEFT)) {
            switch (selectedOption()) {
                case OPTION_PAC_BOOSTER    -> setPrevPacBoosterValue(session);
                case OPTION_DIFFICULTY     -> setPrevDifficultyValue(session);
                case OPTION_MAZE_SELECTION -> setPrevMapCategoryValue(session);
                case OPTION_STARTING_LEVEL -> setPrevStartLevelValue();
            }
        }
        else {
            super.onInput();
        }
    }

    private void setPrevStartLevelValue() {
        final TengenMsPacMan_GamePlay gamePlay = (TengenMsPacMan_GamePlay) game().variant().gamePlay();
        final GameSession session = game().session();

        int current = gamePlay.startLevelNumber(session);
        int prev = (current == MIN_START_LEVEL) ? MAX_START_LEVEL : current - 1;
        gamePlay.setStartLevelNumber(session, prev);

        optionValueChanged();
    }

    private void setNextStartLevelValue() {
        final TengenMsPacMan_GamePlay gamePlay = (TengenMsPacMan_GamePlay) game().variant().gamePlay();
        final GameSession session = game().session();

        int current = gamePlay.startLevelNumber(session);
        int next = (current < MAX_START_LEVEL) ? current + 1 : MIN_START_LEVEL;
        gamePlay.setStartLevelNumber(session, next);

        optionValueChanged();
    }

    private void setPrevMapCategoryValue(GameSession session) {
        final TengenMsPacMan_GamePlay gamePlay = (TengenMsPacMan_GamePlay) game().variant().gamePlay();

        final MapCategory category = gamePlay.mapCategory(session);
        final var values = MapCategory.values();
        final int current = category.ordinal(), prev = (current == 0) ? values.length - 1 :  current - 1;
        gamePlay.setMapCategory(session, values[prev]);

        saveHighScore(session);
        optionValueChanged();
    }

    private void setNextMapCategoryValue(GameSession session) {
        final TengenMsPacMan_GamePlay gamePlay = (TengenMsPacMan_GamePlay) game().variant().gamePlay();

        final MapCategory category = gamePlay.mapCategory(session);
        var values = MapCategory.values();
        int current = category.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        gamePlay.setMapCategory(session, values[next]);

        saveHighScore(session);
        optionValueChanged();
    }

    private void setPrevDifficultyValue(GameSession session) {
        final TengenMsPacMan_GamePlay gamePlay = (TengenMsPacMan_GamePlay) game().variant().gamePlay();

        final Difficulty difficulty = gamePlay.difficulty(session);
        final var values = Difficulty.values();
        final int current = difficulty.ordinal(), prev = (current == 0) ? values.length - 1 : current - 1;
        gamePlay.setDifficulty(game(), values[prev]);

        saveHighScore(session);
        optionValueChanged();
    }

    private void setNextDifficultyValue(GameSession session) {
        final TengenMsPacMan_GamePlay gamePlay = (TengenMsPacMan_GamePlay) game().variant().gamePlay();

        final Difficulty difficulty = gamePlay.difficulty(session);
        final var values = Difficulty.values();
        final int current = difficulty.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        gamePlay.setDifficulty(game(), values[next]);

        saveHighScore(session);
        optionValueChanged();
    }

    private void setPrevPacBoosterValue(GameSession session) {
        final TengenMsPacMan_GamePlay gamePlay = (TengenMsPacMan_GamePlay) game().variant().gamePlay();

        final BoosterMode boosterMode = gamePlay.boosterMode(session);
        final var values = BoosterMode.values();
        final int current = boosterMode.ordinal(), prev = (current == 0) ? values.length - 1 : current - 1;
        gamePlay.setBoosterMode(session, values[prev]);

        optionValueChanged();
    }

    private void setNextPacBoosterValue(GameSession session) {
        final TengenMsPacMan_GamePlay gamePlay = (TengenMsPacMan_GamePlay) game().variant().gamePlay();

        final BoosterMode boosterMode = gamePlay.boosterMode(session);
        final var values = BoosterMode.values();
        final int current = boosterMode.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        gamePlay.setBoosterMode(session, values[next]);

        optionValueChanged();
    }

    private void saveHighScore(GameSession session) {
        try {
            ScoreSystem.save(session.highScore());
        } catch (IOException x) {
            Logger.error(x, "Could not save Tengen Ms. Pac-Man high score");
            //TODO Show message in UI
        }
    }
}