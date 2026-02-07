/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.tengenmspacman.model.Difficulty;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.model.PacBooster;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameController.GameState;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.action.GameAction;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.KeyCode;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_ActionBindings.TENGEN_SPECIFIC_BINDINGS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions.ACTION_START_PLAYING;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions.ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.JOYPAD;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
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
            JOYPAD.selectNextBinding(actionBindings);
        }
    };

    private int idleTicks;
    public int initialDelay;

    public TengenMsPacMan_OptionsScene() {}

    @Override
    public void doInit(Game game) {
        game.hud().hide();

        actionBindings.registerAllFrom(GameUI.SCENE_TESTS_BINDINGS);
        actionBindings.registerByKeyCombination(actionSelectNextJoypadBinding, alt(KeyCode.J));
        actionBindings.registerAnyFrom(ACTION_START_PLAYING,                  TENGEN_SPECIFIC_BINDINGS);
        actionBindings.registerAnyFrom(ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY, TENGEN_SPECIFIC_BINDINGS);

        JOYPAD.setBindings(actionBindings);

        selectedOption.set(OPTION_PAC_BOOSTER);
        tengenGame().setCanStartNewGame(true);

        idleTicks = 0;
        initialDelay = INITIAL_DELAY;
    }

    @Override
    protected void doEnd(Game game) {
        JOYPAD.removeBindings(actionBindings);
    }

    @Override
    public void update(Game game) {
        if (initialDelay > 0) {
            --initialDelay;
            return;
        }
        if (idleTicks < IDLE_TIMEOUT) {
            idleTicks += 1;
        } else {
            game.control().enterState(GameState.INTRO);
        }
    }

    @Override
    public Vector2i unscaledSize() { return NES_SIZE_PX; }

    private TengenMsPacMan_GameModel tengenGame() { return gameContext().currentGame(); }
    
    private void optionValueChanged() {
        ui.soundManager().play("audio.option.value_changed");
        idleTicks = 0;
    }

    public int selectedOption() {
        return selectedOption.get();
    }

    @Override
    public void onKeyboardInput() {
        if (JOYPAD.isButtonPressed(JoypadButton.DOWN)) {
            selectedOption.set(selectedOption() + 1 < NUM_OPTIONS ? selectedOption() + 1 : 0);
        }
        else if (JOYPAD.isButtonPressed(JoypadButton.UP)) {
            selectedOption.set(selectedOption() == 0 ? NUM_OPTIONS - 1 : selectedOption() - 1);
        }
        // Button "A" on the joypad is located right of "B": select next value
        else if (JOYPAD.isButtonPressed(JoypadButton.A) || GameUI.KEYBOARD.isPressed(KeyCode.RIGHT)) {
            switch (selectedOption()) {
                case OPTION_PAC_BOOSTER    -> setNextPacBoosterValue();
                case OPTION_DIFFICULTY     -> setNextDifficultyValue();
                case OPTION_MAZE_SELECTION -> setNextMapCategoryValue();
                case OPTION_STARTING_LEVEL -> setNextStartLevelValue();
            }
        }
        // Button "B" is left of "A": select previous value
        else if (JOYPAD.isButtonPressed(JoypadButton.B) || GameUI.KEYBOARD.isPressed(KeyCode.LEFT)) {
            switch (selectedOption()) {
                case OPTION_PAC_BOOSTER    -> setPrevPacBoosterValue();
                case OPTION_DIFFICULTY     -> setPrevDifficultyValue();
                case OPTION_MAZE_SELECTION -> setPrevMapCategoryValue();
                case OPTION_STARTING_LEVEL -> setPrevStartLevelValue();
            }
        }
        else super.onKeyboardInput();
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
        tengenGame().saveHighScore();
        optionValueChanged();
    }

    private void setNextMapCategoryValue() {
        MapCategory category = tengenGame().mapCategory();
        var values = MapCategory.values();
        int current = category.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        tengenGame().setMapCategory(values[next]);
        tengenGame().saveHighScore();
        optionValueChanged();
    }

    private void setPrevDifficultyValue() {
        Difficulty difficulty = tengenGame().difficulty();
        var values = Difficulty.values();
        int current = difficulty.ordinal(), prev = (current == 0) ? values.length - 1 : current - 1;
        tengenGame().setDifficulty(values[prev]);
        tengenGame().saveHighScore();
        optionValueChanged();
    }

    private void setNextDifficultyValue() {
        Difficulty difficulty = tengenGame().difficulty();
        var values = Difficulty.values();
        int current = difficulty.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        tengenGame().setDifficulty(values[next]);
        tengenGame().saveHighScore();
        optionValueChanged();
    }

    private void setPrevPacBoosterValue() {
        PacBooster pacBooster = tengenGame().pacBooster();
        var values = PacBooster.values();
        int current = pacBooster.ordinal(), prev = (current == 0) ? values.length - 1 : current - 1;
        tengenGame().setPacBooster(values[prev]);
        optionValueChanged();
    }

    private void setNextPacBoosterValue() {
        PacBooster pacBooster = tengenGame().pacBooster();
        var values = PacBooster.values();
        int current = pacBooster.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        tengenGame().setPacBooster(values[next]);
        optionValueChanged();
    }
}