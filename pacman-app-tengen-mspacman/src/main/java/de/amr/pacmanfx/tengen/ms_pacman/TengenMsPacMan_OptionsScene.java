/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.ui.PacManGames_ActionBinding;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_ActionBindings.TENGEN_ACTION_BINDINGS;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_SpriteSheet.sprite;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.*;
import static de.amr.pacmanfx.ui.PacManGames_Action.*;
import static de.amr.pacmanfx.ui.PacManGames_Env.theJoypad;
import static de.amr.pacmanfx.ui.PacManGames_Env.theSound;
import static de.amr.pacmanfx.uilib.input.Keyboard.alt;

/**
 * Options scene for Ms. Pac-Man Tengen.
 *
 * <p></p>The highscore is cleared if player type (1 player, 2 players etc.), map category or difficulty are
 * changed, see <a href="https://github.com/RussianManSMWC/Ms.-Pac-Man-NES-Tengen-Disassembly/blob/main/MsPacManTENGENDis.asm:9545">disassembly</a>.
 *
 * @author Armin Reichert
 */
public class TengenMsPacMan_OptionsScene extends GameScene2D implements PacManGames_ActionBinding {

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
    public void doInit() {
        theGame().setScoreVisible(false);

        bindAction(TengenMsPacMan_Action.ACTION_START_PLAYING, TENGEN_ACTION_BINDINGS);
        bindAction(TengenMsPacMan_Action.ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAYED, TENGEN_ACTION_BINDINGS);
        bindAction(ACTION_TEST_CUT_SCENES, COMMON_ACTION_BINDINGS);
        bindAction(ACTION_TEST_LEVELS_BONI, COMMON_ACTION_BINDINGS);
        bindAction(ACTION_TEST_LEVELS_TEASERS, COMMON_ACTION_BINDINGS);
        bindActionToKeyCombination(() -> theJoypad().selectNextKeyBinding(this), alt(KeyCode.J));

        selectedOption = OPTION_PAC_BOOSTER;
        tengenGame = (TengenMsPacMan_GameModel) theGame();
        tengenGame.setCanStartNewGame(true);
        resetIdleTimer();
        initialDelay = INITIAL_DELAY;
        theJoypad().registerCurrentBinding(this);
    }

    @Override
    protected void doEnd() {
        theJoypad().unregisterCurrentBinding(this);
    }

    @Override
    public void update() {
        if (initialDelay > 0) {
            --initialDelay;
            return;
        }
        if (idleTicks == IDLE_TIMEOUT) {
            theGameController().changeGameState(GameState.INTRO);
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
        theSound().playClipIfEnabled("option.selection_changed", 1);
        resetIdleTimer();
    }

    private void optionValueChanged() {
        theSound().playClipIfEnabled("option.value_changed", 1);
        resetIdleTimer();
    }

    @Override
    public void handleKeyboardInput() {
        if (theJoypad().isButtonPressed(JoypadButton.DOWN)) {
            selectNextOption();
        }
        else if (theJoypad().isButtonPressed(JoypadButton.UP)) {
            selectPrevOption();
        }

        // Button "A" is right of "B": select next value
        else if (theJoypad().isButtonPressed(JoypadButton.A)) {
            switch (selectedOption) {
                case OPTION_PAC_BOOSTER    -> setNextPacBoosterValue();
                case OPTION_DIFFICULTY     -> setNextDifficultyValue();
                case OPTION_MAZE_SELECTION -> setNextMapCategoryValue();
                case OPTION_STARTING_LEVEL -> setNextStartLevelValue();
                default -> {}
            }
        }

        // Button "B" is left of "A": select previous value
        else if (theJoypad().isButtonPressed(JoypadButton.B)) {
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
        tengenGame.saveHighScore();
        optionValueChanged();
    }

    private void setNextMapCategoryValue() {
        MapCategory category = tengenGame.mapCategory();
        var values = MapCategory.values();
        int current = category.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        tengenGame.setMapCategory(values[next]);
        tengenGame.saveHighScore();
        optionValueChanged();
    }

    private void setPrevDifficultyValue() {
        Difficulty difficulty = tengenGame.difficulty();
        var values = Difficulty.values();
        int current = difficulty.ordinal(), prev = (current == 0) ? values.length - 1 : current - 1;
        tengenGame.setDifficulty(values[prev]);
        tengenGame.saveHighScore();
        optionValueChanged();
    }

    private void setNextDifficultyValue() {
        Difficulty difficulty = tengenGame.difficulty();
        var values = Difficulty.values();
        int current = difficulty.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        tengenGame.setDifficulty(values[next]);
        tengenGame.saveHighScore();
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
        var r = (TengenMsPacMan_Renderer2D) gr();
        r.fillCanvas(backgroundColor());

        if (initialDelay > 0) {
            return;
        }

        r.drawVerticalSceneBorders();
        if (PY_TENGEN_JOYPAD_BINDINGS_DISPLAYED.get()) {
            r.drawJoypadKeyBinding(theJoypad().currentKeyBinding());
        }

        r.drawBar(nesPaletteColor(0x20), nesPaletteColor(0x21), sizeInPx().x(), 20);
        r.drawBar(nesPaletteColor(0x20), nesPaletteColor(0x21), sizeInPx().x(), 212);
        r.fillText("MS PAC-MAN OPTIONS", NES_YELLOW, arcadeFont8(), COL_LABEL + 3 * TS, 48);

        // Players (not implemented)
        drawArrowIfSelected(OPTION_PLAYERS, 72, arcadeFont8());
        r.fillText("TYPE", NES_YELLOW, arcadeFont8(), COL_LABEL, 72);
        r.fillText(":", NES_YELLOW, arcadeFont8(), COL_LABEL + 4 * TS + 4, 72);
        r.fillText("1 PLAYER", NES_WHITE, arcadeFont8(), COL_LABEL + 6 * TS  , 72);

        // Pac-Booster
        drawArrowIfSelected(OPTION_PAC_BOOSTER, 96, arcadeFont8());
        r.fillText("PAC BOOSTER", NES_YELLOW, arcadeFont8(), COL_LABEL, 96);
        r.fillText(":", NES_YELLOW, arcadeFont8(), COL_COLON, 96);
        String pacBoosterText = switch (tengenGame.pacBooster()) {
            case OFF -> "OFF";
            case ALWAYS_ON -> "ALWAYS ON";
            case USE_A_OR_B -> "USE A OR B";
        };
        r.fillText(pacBoosterText, NES_WHITE, arcadeFont8(), COL_VALUE, 96);

        // Game difficulty
        drawArrowIfSelected(OPTION_DIFFICULTY, 120, arcadeFont8());
        r.fillText("GAME DIFFICULTY", NES_YELLOW, arcadeFont8(), COL_LABEL, 120);
        r.fillText(":", NES_YELLOW, arcadeFont8(), COL_COLON, 120);
        r.fillText(tengenGame.difficulty().name(), NES_WHITE, arcadeFont8(), COL_VALUE, 120);

        // Maze (type) selection
        drawArrowIfSelected(OPTION_MAZE_SELECTION, 144, arcadeFont8());
        r.fillText("MAZE SELECTION", NES_YELLOW, arcadeFont8(), COL_LABEL, 144);
        r.fillText(":", NES_YELLOW, arcadeFont8(), COL_COLON, 144);
        r.fillText(tengenGame.mapCategory().name(), NES_WHITE, arcadeFont8(), COL_VALUE, 144);

        // Starting level number
        drawArrowIfSelected(OPTION_STARTING_LEVEL, 168, arcadeFont8());
        r.fillText("STARTING LEVEL", NES_YELLOW, arcadeFont8(), COL_LABEL, 168);
        r.fillText(":", NES_YELLOW, arcadeFont8(), COL_COLON, 168);
        r.fillText(String.valueOf(tengenGame.startLevelNumber()), NES_WHITE, arcadeFont8(), COL_VALUE, 168);
        if (tengenGame.numContinues() < 4) {
            RectArea sprite = switch (tengenGame.numContinues()) {
                case 0 -> sprite(SpriteID.CONTINUES_0);
                case 1 -> sprite(SpriteID.CONTINUES_1);
                case 2 -> sprite(SpriteID.CONTINUES_2);
                case 3 -> sprite(SpriteID.CONTINUES_3);
                default -> throw new IllegalArgumentException("Illegal number of continues " + tengenGame.numContinues());
            };
            r.drawSpriteScaled(sprite, COL_VALUE + 3 * TS, 160);
        }

        r.fillText("MOVE ARROW WITH JOYPAD", NES_YELLOW, arcadeFont8(), 4 * TS,  192);
        r.fillText("CHOOSE OPTIONS WITH A AND B", NES_YELLOW, arcadeFont8(), 2 * TS,  200);
        r.fillText("PRESS START TO START GAME", NES_YELLOW, arcadeFont8(), 3 * TS,  208);
    }

    private void drawArrowIfSelected(int option, double y, Font font) {
        if (selectedOption == option) {
            gr().ctx().setFill(NES_YELLOW);
            gr().ctx().fillRect(scaled(COL_ARROW + 2.25), scaled(y - 4.5), scaled(7.5), scaled(1.75));
            gr().fillText(">", NES_YELLOW, font, COL_ARROW + 3, y);
        }
    }
}