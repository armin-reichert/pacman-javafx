/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman.scene;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.nes.NES_JoypadButton;
import de.amr.games.pacman.tengen.ms_pacman.Difficulty;
import de.amr.games.pacman.tengen.ms_pacman.MsPacManGameTengen;
import de.amr.games.pacman.tengen.ms_pacman.PacBooster;
import de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GameActions;
import de.amr.games.pacman.tengen.ms_pacman.maps.MapCategory;
import de.amr.games.pacman.tengen.ms_pacman.rendering2d.TengenMsPacMan_Renderer2D;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.action.GameActions2D;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GameActions.TOGGLE_JOYPAD_BINDINGS_DISPLAYED;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GameConfiguration.NES_SIZE;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GameConfiguration.nesPaletteColor;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GlobalProperties.PY_TENGEN_JOYPAD_BINDINGS_DISPLAYED;
import static de.amr.games.pacman.tengen.ms_pacman.rendering2d.TengenMsPacMan_SpriteSheet.CONTINUES_SPRITES;
import static de.amr.games.pacman.ui2d.input.Keyboard.alt;

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

    static final Color LABEL_COLOR = nesPaletteColor(0x28);
    static final Color VALUE_COLOR = nesPaletteColor(0x20);

    static final int OPTION_PLAYERS = 0;
    static final int OPTION_PAC_BOOSTER = 1;
    static final int OPTION_DIFFICULTY = 2;
    static final int OPTION_MAZE_SELECTION = 3;
    static final int OPTION_STARTING_LEVEL = 4;

    static final int NUM_OPTIONS = 5;

    static final int MIN_START_LEVEL = 1;
    static final int MAX_START_LEVEL = 32;  //TODO 7

    static final int INITIAL_DELAY = 20; //TODO verify
    static final int IDLE_TIMEOUT = 22 * 60; // TODO verify

    private MsPacManGameTengen game;
    private int selectedOption;
    private long idleTicks;
    private int initialDelay;

    @Override
    public void bindGameActions() {
        bind(TengenMsPacMan_GameActions.SELECT_NEXT_JOYPAD_KEY_BINDING, alt(KeyCode.J));
        bind(TengenMsPacMan_GameActions.START_PLAYING, context.currentJoypadKeyBinding().key(NES_JoypadButton.BTN_START));
        bind(TOGGLE_JOYPAD_BINDINGS_DISPLAYED, context.currentJoypadKeyBinding().key(NES_JoypadButton.BTN_SELECT));
        GameActions2D.bindTestActions(this);
    }

    @Override
    public void doInit() {
        context.registerJoypadKeyBinding();
        context.setScoreVisible(false);
        selectedOption = OPTION_PAC_BOOSTER;
        game = (MsPacManGameTengen) context.game();
        game.setCanStartNewGame(true);
        resetIdleTimer();
        initialDelay = INITIAL_DELAY;
    }

    @Override
    protected void doEnd() {
        context.unregisterJoypadKeyBinding();
    }

    @Override
    public void update() {
        if (initialDelay > 0) {
            --initialDelay;
            return;
        }
        if (idleTicks == IDLE_TIMEOUT) {
            context.gameController().changeState(GameState.INTRO);
            return;
        }
        idleTicks += 1;
    }

    @Override
    public Vector2f size() {
        return NES_SIZE.toVector2f();
    }

    private void resetIdleTimer() {
        idleTicks = 0;
    }

    private void optionSelectionChanged() {
        context.sound().playClipIfEnabled("option.selection_changed", 1);
        resetIdleTimer();
    }

    private void optionValueChanged() {
        context.sound().playClipIfEnabled("option.value_changed", 1);
        resetIdleTimer();
    }

    private boolean isJoypadPressed(NES_JoypadButton button) {
        return context.keyboard().isMatching(context.currentJoypadKeyBinding().key(button));
    }

    @Override
    public void handleInput(GameContext context) {

        if (isJoypadPressed(NES_JoypadButton.BTN_DOWN)) {
            selectNextOption();
        }
        else if (isJoypadPressed(NES_JoypadButton.BTN_UP)) {
            selectPrevOption();
        }

        // Button "A" is right of "B": select next value
        else if (isJoypadPressed(NES_JoypadButton.BTN_A)) {
            switch (selectedOption) {
                case OPTION_PAC_BOOSTER    -> setNextPacBoosterValue();
                case OPTION_DIFFICULTY     -> setNextDifficultyValue();
                case OPTION_MAZE_SELECTION -> setNextMapCategoryValue();
                case OPTION_STARTING_LEVEL -> setNextStartLevelValue();
                default -> {}
            }
        }

        // Button "B" is left of "A": select previous value
        else if (isJoypadPressed(NES_JoypadButton.BTN_B)) {
            switch (selectedOption) {
                case OPTION_PAC_BOOSTER    -> setPrevPacBoosterValue();
                case OPTION_DIFFICULTY     -> setPrevDifficultyValue();
                case OPTION_MAZE_SELECTION -> setPrevMapCategoryValue();
                case OPTION_STARTING_LEVEL -> setPrevStartLevelValue();
                default -> {}
            }
        }

        else {
            context.ifGameActionTriggeredRunIt(this);
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
        int current = game.startLevelNumber();
        int prev = (current == MIN_START_LEVEL) ? MAX_START_LEVEL : current - 1;
        game.setStartLevelNumber(prev);
        optionValueChanged();
    }

    private void setNextStartLevelValue() {
        int current = game.startLevelNumber();
        int next = (current < MAX_START_LEVEL) ? current + 1 : MIN_START_LEVEL;
        game.setStartLevelNumber(next);
        optionValueChanged();
    }

    private void setPrevMapCategoryValue() {
        MapCategory category = game.mapCategory();
        var values = MapCategory.values();
        int current = category.ordinal(), prev = (current == 0) ? values.length - 1 :  current - 1;
        game.setMapCategory(values[prev]);
        game.scoreManager().resetHighScore();
        optionValueChanged();
    }

    private void setNextMapCategoryValue() {
        MapCategory category = game.mapCategory();
        var values = MapCategory.values();
        int current = category.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        game.setMapCategory(values[next]);
        game.scoreManager().resetHighScore();
        optionValueChanged();
    }

    private void setPrevDifficultyValue() {
        Difficulty difficulty = game.difficulty();
        var values = Difficulty.values();
        int current = difficulty.ordinal(), prev = (current == 0) ? values.length - 1 : current - 1;
        game.setDifficulty(values[prev]);
        game.scoreManager().resetHighScore();
        optionValueChanged();
    }

    private void setNextDifficultyValue() {
        Difficulty difficulty = game.difficulty();
        var values = Difficulty.values();
        int current = difficulty.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        game.setDifficulty(values[next]);
        game.scoreManager().resetHighScore();
        optionValueChanged();
    }

    private void setPrevPacBoosterValue() {
        PacBooster pacBooster = game.pacBooster();
        var values = PacBooster.values();
        int current = pacBooster.ordinal(), prev = (current == 0) ? values.length - 1 : current - 1;
        game.setPacBooster(values[prev]);
        optionValueChanged();
    }

    private void setNextPacBoosterValue() {
        PacBooster pacBooster = game.pacBooster();
        var values = PacBooster.values();
        int current = pacBooster.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        game.setPacBooster(values[next]);
        optionValueChanged();
    }

    // Drawing

    @Override
    protected void drawSceneContent() {
        TengenMsPacMan_Renderer2D r = (TengenMsPacMan_Renderer2D) gr;
        r.drawSceneBorderLines();

        if (initialDelay > 0) {
            return;
        }
        r.setScaling(scaling());
        Font scaledFont = r.scaledArcadeFont(TS);

        double y = 20;
        r.drawBar(nesPaletteColor(0x20), nesPaletteColor(0x21), size().x(), y);

        y += 28;
        r.drawText("MS PAC-MAN OPTIONS", LABEL_COLOR, scaledFont, COL_LABEL + 3 * TS, y);

        // Players (not implemented)
        y += 3 * TS;
        drawArrowIfSelected(OPTION_PLAYERS, y, scaledFont);
        r.drawText("TYPE", LABEL_COLOR, scaledFont, COL_LABEL, y);
        r.drawText(":", LABEL_COLOR, scaledFont, COL_LABEL + 4 * TS + 4, y);
        r.drawText("1 PLAYER", VALUE_COLOR, scaledFont, COL_LABEL + 6 * TS  , y);

        // Pac-Booster
        y += 3 * TS;
        drawArrowIfSelected(OPTION_PAC_BOOSTER, y, scaledFont);
        r.drawText("PAC BOOSTER", LABEL_COLOR, scaledFont, COL_LABEL, y);
        r.drawText(":", LABEL_COLOR, scaledFont, COL_COLON, y);
        String pacBoosterText = switch (game.pacBooster()) {
            case OFF -> "OFF";
            case ALWAYS_ON -> "ALWAYS ON";
            case USE_A_OR_B -> "USE A OR B";
        };
        r.drawText(pacBoosterText, VALUE_COLOR, scaledFont, COL_VALUE, y);

        // Game difficulty
        y += 3 * TS;
        drawArrowIfSelected(OPTION_DIFFICULTY, y, scaledFont);
        r.drawText("GAME DIFFICULTY", LABEL_COLOR, scaledFont, COL_LABEL, y);
        r.drawText(":", LABEL_COLOR, scaledFont, COL_COLON, y);
        r.drawText(game.difficulty().name(), VALUE_COLOR, scaledFont, COL_VALUE, y);

        // Maze (type) selection
        y += 3 * TS;
        drawArrowIfSelected(OPTION_MAZE_SELECTION, y, scaledFont);
        r.drawText("MAZE SELECTION", LABEL_COLOR, scaledFont, COL_LABEL, y);
        r.drawText(":", LABEL_COLOR, scaledFont, COL_COLON, y);
        r.drawText(game.mapCategory().name(), VALUE_COLOR, scaledFont, COL_VALUE, y);

        // Starting level number
        y += 3 * TS;
        drawArrowIfSelected(OPTION_STARTING_LEVEL, y, scaledFont);
        r.drawText("STARTING LEVEL", LABEL_COLOR, scaledFont, COL_LABEL, y);
        r.drawText(":", LABEL_COLOR, scaledFont, COL_COLON, y);
        r.drawText(String.valueOf(game.startLevelNumber()), VALUE_COLOR, scaledFont, COL_VALUE, y);
        if (game.numContinues() < 4) {
            r.drawSpriteScaled(CONTINUES_SPRITES[game.numContinues()], COL_VALUE + 3 * TS, y - TS);
        }

        y += 3 * TS;
        r.drawText("MOVE ARROW WITH JOYPAD", LABEL_COLOR, scaledFont, 4 * TS,  y);
        y += TS;
        r.drawText("CHOOSE OPTIONS WITH A AND B", LABEL_COLOR, scaledFont, 2 * TS,  y);
        y += TS;
        r.drawText("PRESS START TO START GAME", LABEL_COLOR, scaledFont, 3 * TS,  y);

        y += 4;
        r.drawBar(nesPaletteColor(0x20), nesPaletteColor(0x21), size().x(), y);

        if (PY_TENGEN_JOYPAD_BINDINGS_DISPLAYED.get()) {
            r.drawJoypadBindings(context.currentJoypadKeyBinding());
        }
    }

    private void drawArrowIfSelected(int option, double y, Font font) {
        if (selectedOption == option) {
            gr.drawText("-", LABEL_COLOR, font, COL_ARROW, y);
            gr.drawText(">", LABEL_COLOR, font, COL_ARROW + 3, y);
        }
    }
}