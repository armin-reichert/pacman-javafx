/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.tengenmspacman.model.*;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.input.Input;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.KeyCode;
import org.tinylog.Logger;

import java.io.IOException;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_ActionBindings.TENGEN_SPECIFIC_BINDINGS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions.ACTION_START_PLAYING;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions.ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_WIDTH;
import static de.amr.pacmanfx.ui.input.Keyboard.alt;

/**
 * Options scene for Ms. Pac-Man Tengen.
 *
 * <p></p>The high-score is cleared if player type (1 player, 2 players etc.), map category or difficulty are
 * changed.
 *
 * @see <a href="https://github.com/RussianManSMWC/Ms.-Pac-Man-NES-Tengen-Disassembly/blob/main/MsPacManTENGENDis.asm:9545">Disassembly</a>.
 */
public class TengenMsPacMan_OptionsScene extends GameScene2D {

    public static final Vector2i SIZE = new Vector2i(NES_SCREEN_WIDTH, NES_SCREEN_HEIGHT);

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
            ui.soundManager().play("audio.option.selection_changed");
            idleTicks = 0;
        }
    };

    private final GameAction actionSelectNextJoypadBinding = new GameAction("SELECT_NEXT_JOYPAD_BINDING") {
        @Override
        public void execute(GameUI ui) {
            Input.instance().joypad.selectNextBinding(actionBindings);
        }
    };

    private int idleTicks;
    public int initialDelay;

    public TengenMsPacMan_OptionsScene(GameUI ui) {
        super(ui);
    }

    @Override
    public void onSceneStart() {
        final TengenMsPacMan_GameModel game = gameContext().game();
        game.hud().hide();

        actionBindings.bindOne(ACTION_START_PLAYING, TENGEN_SPECIFIC_BINDINGS);
        actionBindings.bindOne(ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY, TENGEN_SPECIFIC_BINDINGS);
        actionBindings.bind(actionSelectNextJoypadBinding, alt(KeyCode.J));
        actionBindings.bindAll(GameUI.SCENE_TESTS_BINDINGS);

        Input.instance().joypad.setBindings(actionBindings);

        selectedOption.set(OPTION_PAC_BOOSTER);
        game.setCanStartNewGame(true);

        idleTicks = 0;
        initialDelay = INITIAL_DELAY;
    }

    @Override
    public void onSceneEnd() {
        Input.instance().joypad.removeBindings(actionBindings);
    }

    @Override
    public void onTick(long tick) {
        if (initialDelay > 0) {
            --initialDelay;
            return;
        }
        if (idleTicks < IDLE_TIMEOUT) {
            idleTicks += 1;
        } else {
            gameContext().game().flow().enterState(TengenMsPacMan_GameState.INTRO);
        }
    }

    @Override
    public Vector2i unscaledSize() { return SIZE; }

    private TengenMsPacMan_GameModel tengenGame() { return gameContext().game(); }
    
    private void optionValueChanged() {
        ui.soundManager().play("audio.option.value_changed");
        idleTicks = 0;
    }

    public int selectedOption() {
        return selectedOption.get();
    }

    @Override
    public void onUserInput() {
        if (Input.instance().joypad.isButtonPressed(JoypadButton.DOWN)) {
            selectedOption.set(selectedOption() + 1 < NUM_OPTIONS ? selectedOption() + 1 : 0);
        }
        else if (Input.instance().joypad.isButtonPressed(JoypadButton.UP)) {
            selectedOption.set(selectedOption() == 0 ? NUM_OPTIONS - 1 : selectedOption() - 1);
        }
        // Button "A" on the joypad is located right of "B": select next value
        else if (Input.instance().joypad.isButtonPressed(JoypadButton.A)
              || Input.instance().keyboard.isPressed(KeyCode.RIGHT))
        {
            switch (selectedOption()) {
                case OPTION_PAC_BOOSTER    -> setNextPacBoosterValue();
                case OPTION_DIFFICULTY     -> setNextDifficultyValue();
                case OPTION_MAZE_SELECTION -> setNextMapCategoryValue();
                case OPTION_STARTING_LEVEL -> setNextStartLevelValue();
            }
        }
        // Button "B" is left of "A": select previous value
        else if (Input.instance().joypad.isButtonPressed(JoypadButton.B)
              || Input.instance().keyboard.isPressed(KeyCode.LEFT)) {
            switch (selectedOption()) {
                case OPTION_PAC_BOOSTER    -> setPrevPacBoosterValue();
                case OPTION_DIFFICULTY     -> setPrevDifficultyValue();
                case OPTION_MAZE_SELECTION -> setPrevMapCategoryValue();
                case OPTION_STARTING_LEVEL -> setPrevStartLevelValue();
            }
        }
        else actionBindings().matchingAction().ifPresent(action -> action.executeIfEnabled(ui()));
    }

    private void setPrevStartLevelValue() {
        int current = tengenGame().startLevelNumber();
        int prev = (current == MIN_START_LEVEL) ? MAX_START_LEVEL : current - 1;
        tengenGame().setStartLevelNumber(prev);
        optionValueChanged();
    }

    private void setNextStartLevelValue() {
        int current = tengenGame().startLevelNumber();
        int next = (current < MAX_START_LEVEL) ? current + 1 : MIN_START_LEVEL;
        tengenGame().setStartLevelNumber(next);
        optionValueChanged();
    }

    private void setPrevMapCategoryValue() {
        MapCategory category = tengenGame().mapCategory();
        var values = MapCategory.values();
        int current = category.ordinal(), prev = (current == 0) ? values.length - 1 :  current - 1;
        tengenGame().setMapCategory(values[prev]);
        saveHighScore();
        optionValueChanged();
    }

    private void setNextMapCategoryValue() {
        MapCategory category = tengenGame().mapCategory();
        var values = MapCategory.values();
        int current = category.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        tengenGame().setMapCategory(values[next]);
        saveHighScore();
        optionValueChanged();
    }

    private void setPrevDifficultyValue() {
        Difficulty difficulty = tengenGame().difficulty();
        var values = Difficulty.values();
        int current = difficulty.ordinal(), prev = (current == 0) ? values.length - 1 : current - 1;
        tengenGame().setDifficulty(values[prev]);
        saveHighScore();
        optionValueChanged();
    }

    private void setNextDifficultyValue() {
        Difficulty difficulty = tengenGame().difficulty();
        var values = Difficulty.values();
        int current = difficulty.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        tengenGame().setDifficulty(values[next]);
        saveHighScore();
        optionValueChanged();
    }

    private void setPrevPacBoosterValue() {
        PacBooster pacBooster = tengenGame().pacBoosterMode();
        var values = PacBooster.values();
        int current = pacBooster.ordinal(), prev = (current == 0) ? values.length - 1 : current - 1;
        tengenGame().setPacBoosterMode(values[prev]);
        optionValueChanged();
    }

    private void setNextPacBoosterValue() {
        PacBooster pacBooster = tengenGame().pacBoosterMode();
        var values = PacBooster.values();
        int current = pacBooster.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        tengenGame().setPacBoosterMode(values[next]);
        optionValueChanged();
    }

    private void saveHighScore() {
        try {
            tengenGame().highScore().save();
        } catch (IOException x) {
            Logger.error(x, "Could not save Tengen Ms. Pac-Man high score");
            //TODO Show message in UI
        }
    }
}