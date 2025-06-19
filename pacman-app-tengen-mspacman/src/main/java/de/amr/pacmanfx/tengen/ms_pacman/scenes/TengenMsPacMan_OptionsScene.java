/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.tengen.ms_pacman.model.Difficulty;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.tengen.ms_pacman.model.PacBooster;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_GameRenderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.ActionBindingSupport;
import de.amr.pacmanfx.ui.GameAction;
import de.amr.pacmanfx.ui.PacManGames_UI;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.*;
import static de.amr.pacmanfx.ui.PacManGames.*;
import static de.amr.pacmanfx.ui.PacManGames_UI.*;
import static de.amr.pacmanfx.ui.input.Keyboard.alt;

/**
 * Options scene for Ms. Pac-Man Tengen.
 *
 * <p></p>The highscore is cleared if player type (1 player, 2 players etc.), map category or difficulty are
 * changed, see <a href="https://github.com/RussianManSMWC/Ms.-Pac-Man-NES-Tengen-Disassembly/blob/main/MsPacManTENGENDis.asm:9545">disassembly</a>.
 *
 * @author Armin Reichert
 */
public class TengenMsPacMan_OptionsScene extends GameScene2D implements ActionBindingSupport {

    private static final int COL_ARROW = 2 * TS;
    private static final int COL_LABEL = 4 * TS;
    private static final int COL_COLON = 19 * TS;
    private static final int COL_VALUE = 21  * TS;

    private static final Color NES_YELLOW = nesPaletteColor(0x28);
    private static final Color NES_WHITE = nesPaletteColor(0x20);

    private static final byte OPTION_PLAYERS = 0;
    private static final byte OPTION_PAC_BOOSTER = 1;
    private static final byte OPTION_DIFFICULTY = 2;
    private static final byte OPTION_MAZE_SELECTION = 3;
    private static final byte OPTION_STARTING_LEVEL = 4;

    private static final byte NUM_OPTIONS = 5;

    private static final byte MIN_START_LEVEL = 1;
    private static final byte MAX_START_LEVEL = 32;  //TODO 7

    private static final int INITIAL_DELAY = 20; //TODO verify
    private static final int IDLE_TIMEOUT = 1530; // 25,5 sec TODO verify

    private TengenMsPacMan_SpriteSheet spriteSheet;
    private int selectedOption;
    private long idleTicks;
    private int initialDelay;

    private final GameAction actionSelectNextJoypadBinding = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            theJoypad().selectNextKeyBinding(TengenMsPacMan_OptionsScene.this);
        }
    };

    @Override
    public void doInit() {

        theGame().hud().showScore(false);
        theGame().hud().showLevelCounter(false);
        theGame().hud().showLivesCounter(false);

        bindAction(ACTION_START_PLAYING, TENGEN_ACTION_BINDINGS);
        bindAction(ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAYED, TENGEN_ACTION_BINDINGS);
        bindAction(ACTION_TEST_CUT_SCENES, COMMON_ACTION_BINDINGS);
        bindAction(ACTION_TEST_LEVELS_BONI, COMMON_ACTION_BINDINGS);
        bindAction(ACTION_TEST_LEVELS_TEASERS, COMMON_ACTION_BINDINGS);
        bindActionToKeyCombination(actionSelectNextJoypadBinding, alt(KeyCode.J));

        spriteSheet = (TengenMsPacMan_SpriteSheet) theUI().configuration().spriteSheet();
        selectedOption = OPTION_PAC_BOOSTER;
        theTengenGame().setCanStartNewGame(true);
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
    public Vector2f sizeInPx() { return NES_SIZE_PX; }

    private TengenMsPacMan_GameModel theTengenGame() { return (TengenMsPacMan_GameModel) theGame(); }
    
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
        int current = theTengenGame().startLevelNumber();
        int prev = (current == MIN_START_LEVEL) ? MAX_START_LEVEL : current - 1;
        theTengenGame().setStartLevelNumber(prev);
        optionValueChanged();
    }

    private void setNextStartLevelValue() {
        int current = theTengenGame().startLevelNumber();
        int next = (current < MAX_START_LEVEL) ? current + 1 : MIN_START_LEVEL;
        theTengenGame().setStartLevelNumber(next);
        optionValueChanged();
    }

    private void setPrevMapCategoryValue() {
        MapCategory category = theTengenGame().mapCategory();
        var values = MapCategory.values();
        int current = category.ordinal(), prev = (current == 0) ? values.length - 1 :  current - 1;
        theTengenGame().setMapCategory(values[prev]);
        theTengenGame().saveHighScore();
        optionValueChanged();
    }

    private void setNextMapCategoryValue() {
        MapCategory category = theTengenGame().mapCategory();
        var values = MapCategory.values();
        int current = category.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        theTengenGame().setMapCategory(values[next]);
        theTengenGame().saveHighScore();
        optionValueChanged();
    }

    private void setPrevDifficultyValue() {
        Difficulty difficulty = theTengenGame().difficulty();
        var values = Difficulty.values();
        int current = difficulty.ordinal(), prev = (current == 0) ? values.length - 1 : current - 1;
        theTengenGame().setDifficulty(values[prev]);
        theTengenGame().saveHighScore();
        optionValueChanged();
    }

    private void setNextDifficultyValue() {
        Difficulty difficulty = theTengenGame().difficulty();
        var values = Difficulty.values();
        int current = difficulty.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        theTengenGame().setDifficulty(values[next]);
        theTengenGame().saveHighScore();
        optionValueChanged();
    }

    private void setPrevPacBoosterValue() {
        PacBooster pacBooster = theTengenGame().pacBooster();
        var values = PacBooster.values();
        int current = pacBooster.ordinal(), prev = (current == 0) ? values.length - 1 : current - 1;
        theTengenGame().setPacBooster(values[prev]);
        optionValueChanged();
    }

    private void setNextPacBoosterValue() {
        PacBooster pacBooster = theTengenGame().pacBooster();
        var values = PacBooster.values();
        int current = pacBooster.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        theTengenGame().setPacBooster(values[next]);
        optionValueChanged();
    }

    // Drawing

    @Override
    public TengenMsPacMan_GameRenderer gr() {
        return (TengenMsPacMan_GameRenderer) gameRenderer;
    }

    @Override
    protected void drawSceneContent() {
        if (initialDelay > 0) {
            return;
        }

        gr().drawVerticalSceneBorders();
        if (PY_TENGEN_JOYPAD_BINDINGS_DISPLAYED.get()) {
            gr().drawJoypadKeyBinding(theJoypad().currentKeyBinding());
        }

        gr().ctx().setFont(arcadeFont8());

        gr().drawBar(nesPaletteColor(0x20), nesPaletteColor(0x21), sizeInPx().x(), 20);
        gr().drawBar(nesPaletteColor(0x20), nesPaletteColor(0x21), sizeInPx().x(), 212);
        gr().fillTextAtScaledPosition("MS PAC-MAN OPTIONS", NES_YELLOW, COL_LABEL + 3 * TS, 48);

        // Players (not implemented)
        drawArrowAtSelectedOption(OPTION_PLAYERS, 72, arcadeFont8());
        gr().fillTextAtScaledPosition("TYPE", NES_YELLOW, COL_LABEL, 72);
        gr().fillTextAtScaledPosition(":", NES_YELLOW, COL_LABEL + 4 * TS + 4, 72);
        gr().fillTextAtScaledPosition("1 PLAYER", NES_WHITE, COL_LABEL + 6 * TS  , 72);

        // Pac-Booster
        drawArrowAtSelectedOption(OPTION_PAC_BOOSTER, 96, arcadeFont8());
        gr().fillTextAtScaledPosition("PAC BOOSTER", NES_YELLOW, COL_LABEL, 96);
        gr().fillTextAtScaledPosition(":", NES_YELLOW, COL_COLON, 96);
        String pacBoosterText = switch (theTengenGame().pacBooster()) {
            case OFF -> "OFF";
            case ALWAYS_ON -> "ALWAYS ON";
            case USE_A_OR_B -> "USE A OR B";
        };
        gr().fillTextAtScaledPosition(pacBoosterText, NES_WHITE, COL_VALUE, 96);

        // Game difficulty
        drawArrowAtSelectedOption(OPTION_DIFFICULTY, 120, arcadeFont8());
        gr().fillTextAtScaledPosition("GAME DIFFICULTY", NES_YELLOW, COL_LABEL, 120);
        gr().fillTextAtScaledPosition(":", NES_YELLOW, COL_COLON, 120);
        gr().fillTextAtScaledPosition(theTengenGame().difficulty().name(), NES_WHITE, COL_VALUE, 120);

        // Maze (type) selection
        drawArrowAtSelectedOption(OPTION_MAZE_SELECTION, 144, arcadeFont8());
        gr().fillTextAtScaledPosition("MAZE SELECTION", NES_YELLOW, COL_LABEL, 144);
        gr().fillTextAtScaledPosition(":", NES_YELLOW, COL_COLON, 144);
        gr().fillTextAtScaledPosition(theTengenGame().mapCategory().name(), NES_WHITE, COL_VALUE, 144);

        // Starting level number
        drawArrowAtSelectedOption(OPTION_STARTING_LEVEL, 168, arcadeFont8());
        gr().fillTextAtScaledPosition("STARTING LEVEL", NES_YELLOW, COL_LABEL, 168);
        gr().fillTextAtScaledPosition(":", NES_YELLOW, COL_COLON, 168);
        gr().fillTextAtScaledPosition(String.valueOf(theTengenGame().startLevelNumber()), NES_WHITE, COL_VALUE, 168);
        if (theTengenGame().numContinues() < 4) {
            Sprite continuesSprite = spriteSheet.sprite(switch (theTengenGame().numContinues()) {
                case 0 -> SpriteID.CONTINUES_0;
                case 1 -> SpriteID.CONTINUES_1;
                case 2 -> SpriteID.CONTINUES_2;
                case 3 -> SpriteID.CONTINUES_3;
                default -> throw new IllegalArgumentException("Illegal number of continues: " + theTengenGame().numContinues());
            });
            gr().drawSpriteScaled(continuesSprite, COL_VALUE + 3 * TS, 160);
        }

        gr().fillTextAtScaledPosition("MOVE ARROW WITH JOYPAD", NES_YELLOW, 4 * TS,  192);
        gr().fillTextAtScaledPosition("CHOOSE OPTIONS WITH A AND B", NES_YELLOW, 2 * TS,  200);
        gr().fillTextAtScaledPosition("PRESS START TO START GAME", NES_YELLOW, 3 * TS,  208);
    }

    private void drawArrowAtSelectedOption(int option, double y, Font font) {
        if (selectedOption == option) {
            ctx().setFill(NES_YELLOW);
            ctx().fillRect(scaled(COL_ARROW + 2.25), scaled(y - 4.5), scaled(7.5), scaled(1.75));
            gr().fillTextAtScaledPosition(">", NES_YELLOW, font, COL_ARROW + 3, y);
        }
    }
}