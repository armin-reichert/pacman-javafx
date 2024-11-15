/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.model.ms_pacman_tengen.BoosterMode;
import de.amr.games.pacman.model.ms_pacman_tengen.Difficulty;
import de.amr.games.pacman.model.ms_pacman_tengen.MapCategory;
import de.amr.games.pacman.model.ms_pacman_tengen.TengenMsPacManGame;
import de.amr.games.pacman.ui2d.GameActions2D;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui2d.input.Keyboard.alt;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameSceneConfig.paletteColor;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameSceneConfig.*;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameSpriteSheet.CONTINUES_SPRITES;

/**
 * @author Armin Reichert
 */
public class OptionsScene extends GameScene2D {

    static final int COL_ARROW = 2 * TS;
    static final int COL_LABEL = 4 * TS;
    static final int COL_COLON = 19 * TS;
    static final int COL_VALUE = 21  * TS;

    static final Color LABEL_COLOR = paletteColor(0x28);
    static final Color VALUE_COLOR = paletteColor(0x20);

    static final int OPTION_PLAYERS = 0;
    static final int OPTION_PAC_BOOSTER = 1;
    static final int OPTION_DIFFICULTY = 2;
    static final int OPTION_MAZE_SELECTION = 3;
    static final int OPTION_STARTING_LEVEL = 4;

    static final int NUM_OPTIONS = 5;

    static final int MIN_START_LEVEL = 1;
    static final int MAX_START_LEVEL = 32;  //TODO 7

    private TengenMsPacManGame game;
    private int selectedOption;
    private long idleTicks;

    @Override
    public void bindGameActions() {
        bind(TengenGameActions.SELECT_NEXT_JOYPAD, alt(KeyCode.J));
        bind(TengenGameActions.START_PLAYING,      context.joypad().keyCombination(NES.Joypad.START));
        GameActions2D.bindTestActions(this);
    }

    @Override
    public void doInit() {
        context.enableJoypad();
        context.setScoreVisible(false);
        selectedOption = OPTION_PAC_BOOSTER;
        game = (TengenMsPacManGame) context.game();
        game.setCanStartGame(true);
        resetIdleTimer();
    }

    @Override
    protected void doEnd() {
        context.disableJoypad();
    }

    @Override
    public void update() {
        if (idleTicks == 25*60) { // TODO check exact time in disassembly
            context.gameController().changeState(GameState.INTRO);
            return;
        }
        idleTicks += 1;
    }

    @Override
    public Vector2f size() {
        return new Vector2f(NES_RESOLUTION_X, NES_RESOLUTION_Y);
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

    private boolean isJoypadPressed(NES.Joypad button) {
        return context.keyboard().isMatching(context.joypad().keyCombination(button));
    }

    @Override
    public void handleInput(GameContext context) {

        if (isJoypadPressed(NES.Joypad.DOWN)) {
            selectNextOption();
        }
        else if (isJoypadPressed(NES.Joypad.UP)) {
            selectPrevOption();
        }

        // Button "A" is right of "B": select next value
        else if (isJoypadPressed(NES.Joypad.A)) {
            switch (selectedOption) {
                case OPTION_PAC_BOOSTER    -> setNextPacBoosterValue();
                case OPTION_DIFFICULTY     -> setNextDifficultyValue();
                case OPTION_MAZE_SELECTION -> setNextMapCategoryValue();
                case OPTION_STARTING_LEVEL -> setNextStartLevelValue();
                default -> {}
            }
        }

        // Button "B" is left of "A": select previous value
        else if (isJoypadPressed(NES.Joypad.B)) {
            switch (selectedOption) {
                case OPTION_PAC_BOOSTER    -> setPrevPacBoosterValue();
                case OPTION_DIFFICULTY     -> setPrevDifficultyValue();
                case OPTION_MAZE_SELECTION -> setPrevMapCategoryValue();
                case OPTION_STARTING_LEVEL -> setPrevStartLevelValue();
                default -> {}
            }
        }

        else {
            context.ifGameActionRun(this);
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
        optionValueChanged();
    }

    private void setNextMapCategoryValue() {
        MapCategory category = game.mapCategory();
        var values = MapCategory.values();
        int current = category.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        game.setMapCategory(values[next]);
        optionValueChanged();
    }

    private void setPrevDifficultyValue() {
        Difficulty difficulty = game.difficulty();
        var values = Difficulty.values();
        int current = difficulty.ordinal(), prev = (current == 0) ? values.length - 1 : current - 1;
        game.setDifficulty(values[prev]);
        optionValueChanged();
    }

    private void setNextDifficultyValue() {
        Difficulty difficulty = game.difficulty();
        var values = Difficulty.values();
        int current = difficulty.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        game.setDifficulty(values[next]);
        optionValueChanged();
    }

    private void setPrevPacBoosterValue() {
        BoosterMode boosterMode = game.boosterMode();
        var values = BoosterMode.values();
        int current = boosterMode.ordinal(), prev = (current == 0) ? values.length - 1 : current - 1;
        game.setBoosterMode(values[prev]);
        optionValueChanged();
    }

    private void setNextPacBoosterValue() {
        BoosterMode boosterMode = game.boosterMode();
        var values = BoosterMode.values();
        int current = boosterMode.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        game.setBoosterMode(values[next]);
        optionValueChanged();
    }

    // Drawing

    @Override
    protected void drawSceneContent(GameRenderer renderer) {
        TengenMsPacManGameRenderer r = (TengenMsPacManGameRenderer) renderer;

        r.setScaling(scaling());
        Font scaledFont = r.scaledArcadeFont(TS);

        double y = 20;
        r.drawBar(paletteColor(0x20), paletteColor(0x21), size().x(), y);

        y += 28;
        renderer.drawText("MS PAC-MAN OPTIONS", LABEL_COLOR, scaledFont, COL_LABEL + 3 * TS, y);

        // Players (not implemented)
        y += 3 * TS;
        drawArrowIfSelected(renderer, OPTION_PLAYERS, y, scaledFont);
        renderer.drawText("TYPE", LABEL_COLOR, scaledFont, COL_LABEL, y);
        renderer.drawText(":", LABEL_COLOR, scaledFont, COL_LABEL + 4 * TS + 4, y);
        renderer.drawText("1 PLAYER", VALUE_COLOR, scaledFont, COL_LABEL + 6 * TS  , y);

        // Pac-Booster
        y += 3 * TS;
        drawArrowIfSelected(renderer, OPTION_PAC_BOOSTER, y, scaledFont);
        renderer.drawText("PAC BOOSTER", LABEL_COLOR, scaledFont, COL_LABEL, y);
        renderer.drawText(":", LABEL_COLOR, scaledFont, COL_COLON, y);
        String pacBoosterText = switch (game.boosterMode()) {
            case OFF -> "OFF";
            case ALWAYS_ON -> "ALWAYS ON";
            case ACTIVATED_USING_KEY -> "USE A OR B";
        };
        renderer.drawText(pacBoosterText, VALUE_COLOR, scaledFont, COL_VALUE, y);

        // Game difficulty
        y += 3 * TS;
        drawArrowIfSelected(renderer, OPTION_DIFFICULTY, y, scaledFont);
        renderer.drawText("GAME DIFFICULTY", LABEL_COLOR, scaledFont, COL_LABEL, y);
        renderer.drawText(":", LABEL_COLOR, scaledFont, COL_COLON, y);
        renderer.drawText(game.difficulty().name(), VALUE_COLOR, scaledFont, COL_VALUE, y);

        // Maze (type) selection
        y += 3 * TS;
        drawArrowIfSelected(renderer, OPTION_MAZE_SELECTION, y, scaledFont);
        renderer.drawText("MAZE SELECTION", LABEL_COLOR, scaledFont, COL_LABEL, y);
        renderer.drawText(":", LABEL_COLOR, scaledFont, COL_COLON, y);
        renderer.drawText(game.mapCategory().name(), VALUE_COLOR, scaledFont, COL_VALUE, y);

        // Starting level number
        y += 3 * TS;
        drawArrowIfSelected(renderer, OPTION_STARTING_LEVEL, y, scaledFont);
        renderer.drawText("STARTING LEVEL", LABEL_COLOR, scaledFont, COL_LABEL, y);
        renderer.drawText(":", LABEL_COLOR, scaledFont, COL_COLON, y);
        renderer.drawText(String.valueOf(game.startLevelNumber()), VALUE_COLOR, scaledFont, COL_VALUE, y);
        if (game.numContinues() < 4) {
            renderer.drawSpriteScaled(CONTINUES_SPRITES[game.numContinues()], COL_VALUE + 3 * TS, y - TS);
        }

        y += 3 * TS;
        centerLabelText(renderer, "MOVE ARROW WITH JOYPAD", scaledFont, y);
        y += TS;
        centerLabelText(renderer, "CHOOSE OPTIONS WITH A AND B", scaledFont, y);
        y += TS;
        centerLabelText(renderer, "PRESS START TO START GAME", scaledFont, y);

        y += 4;
        r.drawBar(paletteColor(0x20), paletteColor(0x21), size().x(), y);
    }

    private void centerLabelText(GameRenderer renderer, String text, Font font, double y) {
        renderer.drawText(text, LABEL_COLOR, font, (NES_TILES_X - text.length()) * HTS, y);
    }

    private void drawArrowIfSelected(GameRenderer renderer, int option, double y, Font font) {
        if (selectedOption == option) {
            renderer.drawText("-", LABEL_COLOR, font, COL_ARROW, y);
            renderer.drawText(">", LABEL_COLOR, font, COL_ARROW + 3, y);
        }
    }
}