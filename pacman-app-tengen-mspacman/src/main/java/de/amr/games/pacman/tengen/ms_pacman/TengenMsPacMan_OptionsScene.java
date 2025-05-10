/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.nes.JoypadButtonID;
import de.amr.games.pacman.ui._2d.GameScene2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GameAction.START_PLAYING;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GameAction.TOGGLE_JOYPAD_BINDINGS_DISPLAYED;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_SpriteSheet.CONTINUES_SPRITES;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_UIConfig.*;
import static de.amr.games.pacman.ui.Globals.THE_JOYPAD;
import static de.amr.games.pacman.ui.Globals.THE_SOUND;
import static de.amr.games.pacman.uilib.input.Keyboard.alt;

/**
 * Options scene for Ms. Pac-Man Tengen.
 *
 * <p></p>The highscore is cleared if player type (1 player, 2 players etc.), map category or difficulty are
 * changed, see <a href="https://github.com/RussianManSMWC/Ms.-Pac-Man-NES-Tengen-Disassembly/blob/main/MsPacManTENGENDis.asm:9545">disassembly</a>.
 *
 * @author Armin Reichert
 */
public class TengenMsPacMan_OptionsScene extends GameScene2D {

    static final int COL_ARROW = 2 * TS;
    static final int COL_LABEL = 4 * TS;
    static final int COL_COLON = 19 * TS;
    static final int COL_VALUE = 21  * TS;

    static final Color NES_YELLOW = nesPaletteColor(0x28);
    static final Color NES_WHITE = nesPaletteColor(0x20);

    static final int OPTION_PLAYERS = 0;
    static final int OPTION_PAC_BOOSTER = 1;
    static final int OPTION_DIFFICULTY = 2;
    static final int OPTION_MAZE_SELECTION = 3;
    static final int OPTION_STARTING_LEVEL = 4;

    static final int NUM_OPTIONS = 5;

    static final int MIN_START_LEVEL = 1;
    static final int MAX_START_LEVEL = 32;  //TODO 7

    static final int INITIAL_DELAY = 20; //TODO verify
    static final int IDLE_TIMEOUT = 1530; // 25,5 sec TODO verify

    private TengenMsPacMan_GameModel tengenGame;
    private int selectedOption;
    private long idleTicks;
    private int initialDelay;

    @Override
    public void bindActions() {
        bind(() -> THE_JOYPAD.selectNextKeyBinding(this), alt(KeyCode.J));
        bind(START_PLAYING, THE_JOYPAD.key(JoypadButtonID.START));
        bind(TOGGLE_JOYPAD_BINDINGS_DISPLAYED, THE_JOYPAD.key(JoypadButtonID.SELECT));
        bindStartTestsActions();
    }

    @Override
    public void doInit() {
        game().scoreManager().setScoreVisible(false);
        selectedOption = OPTION_PAC_BOOSTER;
        tengenGame = (TengenMsPacMan_GameModel) game();
        tengenGame.setCanStartNewGame(true);
        resetIdleTimer();
        initialDelay = INITIAL_DELAY;
        THE_JOYPAD.registerCurrentBinding(this);
    }

    @Override
    protected void doEnd() {
        THE_JOYPAD.unregisterCurrentBinding(this);
    }

    @Override
    public void update() {
        if (initialDelay > 0) {
            --initialDelay;
            return;
        }
        if (idleTicks == IDLE_TIMEOUT) {
            THE_GAME_CONTROLLER.changeState(GameState.INTRO);
            return;
        }
        idleTicks += 1;
    }

    @Override
    public Vector2f sizeInPx() {
        return NES_SIZE.toVector2f();
    }

    private void resetIdleTimer() {
        idleTicks = 0;
    }

    private void optionSelectionChanged() {
        THE_SOUND.playClipIfEnabled("option.selection_changed", 1);
        resetIdleTimer();
    }

    private void optionValueChanged() {
        THE_SOUND.playClipIfEnabled("option.value_changed", 1);
        resetIdleTimer();
    }

    @Override
    public void handleKeyboardInput() {

        if (THE_JOYPAD.isButtonPressed(JoypadButtonID.DOWN)) {
            selectNextOption();
        }
        else if (THE_JOYPAD.isButtonPressed(JoypadButtonID.UP)) {
            selectPrevOption();
        }

        // Button "A" is right of "B": select next value
        else if (THE_JOYPAD.isButtonPressed(JoypadButtonID.A)) {
            switch (selectedOption) {
                case OPTION_PAC_BOOSTER    -> setNextPacBoosterValue();
                case OPTION_DIFFICULTY     -> setNextDifficultyValue();
                case OPTION_MAZE_SELECTION -> setNextMapCategoryValue();
                case OPTION_STARTING_LEVEL -> setNextStartLevelValue();
                default -> {}
            }
        }

        // Button "B" is left of "A": select previous value
        else if (THE_JOYPAD.isButtonPressed(JoypadButtonID.B)) {
            switch (selectedOption) {
                case OPTION_PAC_BOOSTER    -> setPrevPacBoosterValue();
                case OPTION_DIFFICULTY     -> setPrevDifficultyValue();
                case OPTION_MAZE_SELECTION -> setPrevMapCategoryValue();
                case OPTION_STARTING_LEVEL -> setPrevStartLevelValue();
                default -> {}
            }
        }

        else {
            super.handleKeyboardInput();
        }
    }

    private void selectPrevOption() {
        selectedOption = selectedOption == 0 ? NUM_OPTIONS - 1 : selectedOption - 1;
        optionSelectionChanged();
    }

    private void selectNextOption() {
        selectedOption = (selectedOption < NUM_OPTIONS - 1) ? selectedOption + 1 : 0;
        optionSelectionChanged();
    }

    private void setPrevStartLevelValue() {
        int current = tengenGame.startLevelNumber();
        int prev = (current == MIN_START_LEVEL) ? MAX_START_LEVEL : current - 1;
        tengenGame.setStartLevelNumber(prev);
        optionValueChanged();
    }

    private void setNextStartLevelValue() {
        int current = tengenGame.startLevelNumber();
        int next = (current < MAX_START_LEVEL) ? current + 1 : MIN_START_LEVEL;
        tengenGame.setStartLevelNumber(next);
        optionValueChanged();
    }

    private void setPrevMapCategoryValue() {
        MapCategory category = tengenGame.mapCategory();
        var values = MapCategory.values();
        int current = category.ordinal(), prev = (current == 0) ? values.length - 1 :  current - 1;
        tengenGame.setMapCategory(values[prev]);
        tengenGame.scoreManager().saveHighScore();
        optionValueChanged();
    }

    private void setNextMapCategoryValue() {
        MapCategory category = tengenGame.mapCategory();
        var values = MapCategory.values();
        int current = category.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        tengenGame.setMapCategory(values[next]);
        tengenGame.scoreManager().saveHighScore();
        optionValueChanged();
    }

    private void setPrevDifficultyValue() {
        Difficulty difficulty = tengenGame.difficulty();
        var values = Difficulty.values();
        int current = difficulty.ordinal(), prev = (current == 0) ? values.length - 1 : current - 1;
        tengenGame.setDifficulty(values[prev]);
        tengenGame.scoreManager().saveHighScore();
        optionValueChanged();
    }

    private void setNextDifficultyValue() {
        Difficulty difficulty = tengenGame.difficulty();
        var values = Difficulty.values();
        int current = difficulty.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        tengenGame.setDifficulty(values[next]);
        tengenGame.scoreManager().saveHighScore();
        optionValueChanged();
    }

    private void setPrevPacBoosterValue() {
        PacBooster pacBooster = tengenGame.pacBooster();
        var values = PacBooster.values();
        int current = pacBooster.ordinal(), prev = (current == 0) ? values.length - 1 : current - 1;
        tengenGame.setPacBooster(values[prev]);
        optionValueChanged();
    }

    private void setNextPacBoosterValue() {
        PacBooster pacBooster = tengenGame.pacBooster();
        var values = PacBooster.values();
        int current = pacBooster.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        tengenGame.setPacBooster(values[next]);
        optionValueChanged();
    }

    // Drawing

    @Override
    protected void drawSceneContent() {
        final Font font = arcadeFontScaledTS();
        gr.fillCanvas(backgroundColor());
        gr.drawScores(game().scoreManager(), nesPaletteColor(0x20), font);
        TengenMsPacMan_Renderer2D r = (TengenMsPacMan_Renderer2D) gr;
        r.drawSceneBorderLines();

        if (initialDelay > 0) {
            return;
        }
        r.setScaling(scaling());
        r.drawBar(nesPaletteColor(0x20), nesPaletteColor(0x21), sizeInPx().x(), 20);
        r.fillTextAtScaledPosition("MS PAC-MAN OPTIONS", NES_YELLOW, font, COL_LABEL + 3 * TS, 48);

        // Players (not implemented)
        drawArrowIfSelected(OPTION_PLAYERS, 72, font);
        r.fillTextAtScaledPosition("TYPE", NES_YELLOW, font, COL_LABEL, 72);
        r.fillTextAtScaledPosition(":", NES_YELLOW, font, COL_LABEL + 4 * TS + 4, 72);
        r.fillTextAtScaledPosition("1 PLAYER", NES_WHITE, font, COL_LABEL + 6 * TS  , 72);

        // Pac-Booster
        drawArrowIfSelected(OPTION_PAC_BOOSTER, 96, font);
        r.fillTextAtScaledPosition("PAC BOOSTER", NES_YELLOW, font, COL_LABEL, 96);
        r.fillTextAtScaledPosition(":", NES_YELLOW, font, COL_COLON, 96);
        String pacBoosterText = switch (tengenGame.pacBooster()) {
            case OFF -> "OFF";
            case ALWAYS_ON -> "ALWAYS ON";
            case USE_A_OR_B -> "USE A OR B";
        };
        r.fillTextAtScaledPosition(pacBoosterText, NES_WHITE, font, COL_VALUE, 96);

        // Game difficulty
        drawArrowIfSelected(OPTION_DIFFICULTY, 120, font);
        r.fillTextAtScaledPosition("GAME DIFFICULTY", NES_YELLOW, font, COL_LABEL, 120);
        r.fillTextAtScaledPosition(":", NES_YELLOW, font, COL_COLON, 120);
        r.fillTextAtScaledPosition(tengenGame.difficulty().name(), NES_WHITE, font, COL_VALUE, 120);

        // Maze (type) selection
        drawArrowIfSelected(OPTION_MAZE_SELECTION, 144, font);
        r.fillTextAtScaledPosition("MAZE SELECTION", NES_YELLOW, font, COL_LABEL, 144);
        r.fillTextAtScaledPosition(":", NES_YELLOW, font, COL_COLON, 144);
        r.fillTextAtScaledPosition(tengenGame.mapCategory().name(), NES_WHITE, font, COL_VALUE, 144);

        // Starting level number
        drawArrowIfSelected(OPTION_STARTING_LEVEL, 168, font);
        r.fillTextAtScaledPosition("STARTING LEVEL", NES_YELLOW, font, COL_LABEL, 168);
        r.fillTextAtScaledPosition(":", NES_YELLOW, font, COL_COLON, 168);
        r.fillTextAtScaledPosition(String.valueOf(tengenGame.startLevelNumber()), NES_WHITE, font, COL_VALUE, 168);
        if (tengenGame.numContinues() < 4) {
            r.drawSpriteScaled(CONTINUES_SPRITES[tengenGame.numContinues()], COL_VALUE + 3 * TS, 160);
        }

        r.fillTextAtScaledPosition("MOVE ARROW WITH JOYPAD", NES_YELLOW, font, 4 * TS,  192);
        r.fillTextAtScaledPosition("CHOOSE OPTIONS WITH A AND B", NES_YELLOW, font, 2 * TS,  200);
        r.fillTextAtScaledPosition("PRESS START TO START GAME", NES_YELLOW, font, 3 * TS,  208);

        r.drawBar(nesPaletteColor(0x20), nesPaletteColor(0x21), sizeInPx().x(), 212);

        if (PY_TENGEN_JOYPAD_BINDINGS_DISPLAYED.get()) {
            r.drawJoypadKeyBinding(THE_JOYPAD.currentKeyBinding());
        }
    }

    private void drawArrowIfSelected(int option, double y, Font font) {
        if (selectedOption == option) {
            gr.fillTextAtScaledPosition("-", NES_YELLOW, font, COL_ARROW, y);
            gr.fillTextAtScaledPosition(">", NES_YELLOW, font, COL_ARROW + 3, y);
        }
    }
}