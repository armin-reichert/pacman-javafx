/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.ms_pacman_tengen.BoosterMode;
import de.amr.games.pacman.model.ms_pacman_tengen.Difficulty;
import de.amr.games.pacman.model.ms_pacman_tengen.MapCategory;
import de.amr.games.pacman.model.ms_pacman_tengen.TengenMsPacManGame;
import de.amr.games.pacman.ui2d.GameActions2D;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameRenderer.TENGEN_BABY_BLUE;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameRenderer.TENGEN_YELLOW;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameSceneConfiguration.NES_RESOLUTION_X;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameSceneConfiguration.NES_RESOLUTION_Y;
import static de.amr.games.pacman.ui2d.util.KeyInput.alt;
import static de.amr.games.pacman.ui2d.util.KeyInput.shift_alt;

/**
 * @author Armin Reichert
 */
public class OptionsScene extends GameScene2D {

    static final int COL_ARROW = 2 * TS;
    static final int COL_LABEL = 4 * TS;
    static final int COL_COLON = 19 * TS;
    static final int COL_VALUE = 21  * TS;

    static final Color LABEL_COLOR = TENGEN_YELLOW;
    static final Color VALUE_COLOR = Color.WHITE;

    static final int OPTION_PLAYERS = 0;
    static final int OPTION_PAC_BOOSTER = 1;
    static final int OPTION_DIFFICULTY = 2;
    static final int OPTION_MAZE_SELECTION = 3;
    static final int OPTION_STARTING_LEVEL = 4;

    static final int NUM_OPTIONS = 5;

    static final int MIN_START_LEVEL = 1;
    static final int MAX_START_LEVEL = 32;  //TODO 7

    private TengenMsPacManGame tengenGame;
    private int selectedOption;
    private long idleTicks;

    @Override
    public void defineGameActionKeyBindings() {
        bindAction(GameActions2D.TEST_CUT_SCENES,     alt(KeyCode.C));
        bindAction(GameActions2D.TEST_LEVELS_BONI,    alt(KeyCode.T));
        bindAction(GameActions2D.TEST_LEVELS_TEASERS, shift_alt(KeyCode.T));
        bindAction(GameActions2D.TENGEN_SELECT_NEXT_JOYPAD, alt(KeyCode.J));
    }

    @Override
    public void doInit() {
        context.enableJoypad();
        context.setScoreVisible(false);
        selectedOption = OPTION_PAC_BOOSTER;
        tengenGame = (TengenMsPacManGame) context.game();
        tengenGame.setCanStartGame(true);
        resetIdleTimer();
    }

    @Override
    protected void doEnd() {
        context.disableJoypad();
    }

    @Override
    public void update() {
        if (idleTicks == 25*60) { // TODO check idle time in disassembly
            context.gameController().changeState(GameState.INTRO);
            return;
        }
        idleTicks += 1;
    }

    @Override
    public Vector2f size() {
        return new Vector2f(NES_RESOLUTION_X, NES_RESOLUTION_Y);
    }
    @Override

    protected void drawSceneContent(GameRenderer renderer) {
        renderer.scalingProperty().set(scaling());
        Font font = renderer.scaledArcadeFont(TS);

        double y = 20;
        drawBabyBlueBar(renderer, y);

        y += 28;
        renderer.drawText("MS PAC-MAN OPTIONS", LABEL_COLOR, font, COL_LABEL + 3 * TS, y);

        // Players (not implemented)
        y += 3 * TS;
        drawArrowIfSelected(renderer, OPTION_PLAYERS, y, font);
        renderer.drawText("TYPE", LABEL_COLOR, font, COL_LABEL, y);
        renderer.drawText(":", LABEL_COLOR, font, COL_LABEL + 4 * TS + 4, y);
        renderer.drawText("1 PLAYER", VALUE_COLOR, font, COL_LABEL + 6 * TS  , y);

        // Pac-Booster
        y += 3 * TS;
        drawArrowIfSelected(renderer, OPTION_PAC_BOOSTER, y, font);
        renderer.drawText("PAC BOOSTER", LABEL_COLOR, font, COL_LABEL, y);
        renderer.drawText(":", LABEL_COLOR, font, COL_COLON, y);
        renderer.drawText(pacBoosterText(tengenGame.boosterMode()), VALUE_COLOR, font, COL_VALUE, y);

        // Game difficulty
        y += 3 * TS;
        drawArrowIfSelected(renderer, OPTION_DIFFICULTY, y, font);
        renderer.drawText("GAME DIFFICULTY", LABEL_COLOR, font, COL_LABEL, y);
        renderer.drawText(":", LABEL_COLOR, font, COL_COLON, y);
        renderer.drawText(tengenGame.difficulty().name(), VALUE_COLOR, font, COL_VALUE, y);

        // Maze (type) selection
        y += 3 * TS;
        drawArrowIfSelected(renderer, OPTION_MAZE_SELECTION, y, font);
        renderer.drawText("MAZE SELECTION", LABEL_COLOR, font, COL_LABEL, y);
        renderer.drawText(":", LABEL_COLOR, font, COL_COLON, y);
        renderer.drawText(tengenGame.mapCategory().name(), VALUE_COLOR, font, COL_VALUE, y);

        // Starting level number
        y += 3 * TS;
        drawArrowIfSelected(renderer, OPTION_STARTING_LEVEL, y, font);
        renderer.drawText("STARTING LEVEL", LABEL_COLOR, font, COL_LABEL, y);
        renderer.drawText(":", LABEL_COLOR, font, COL_COLON, y);
        renderer.drawText(String.valueOf(tengenGame.startLevelNumber()), VALUE_COLOR, font, COL_VALUE + TS, y);

        y += 3 * TS;
        drawCenteredText(renderer, "MOVE ARROW WITH JOYPAD", font, y);
        y += TS;
        drawCenteredText(renderer, "CHOOSE OPTIONS WITH A AND B", font, y);
        y += TS;
        drawCenteredText(renderer, "PRESS START TO START GAME", font, y);

        y += 4;
        drawBabyBlueBar(renderer, y);
    }

    private void drawBabyBlueBar(GameRenderer renderer, double y) {
        GraphicsContext g = renderer.ctx();
        g.save();
        g.scale(scaling(), scaling());
        g.setFill(Color.WHITE);
        g.fillRect(0, y, size().x(), TS);
        g.setFill(TENGEN_BABY_BLUE);
        g.fillRect(0, y + 1, size().x(), TS - 2);
        g.restore();
    }

    private String pacBoosterText(BoosterMode boosterMode) {
        return switch (boosterMode) {
            case OFF -> "OFF";
            case ALWAYS_ON -> "ALWAYS ON";
            case ACTIVATED_USING_KEY -> "KEY A";
        };
    }

    private void drawCenteredText(GameRenderer renderer, String text, Font font, double y) {
        double sceneWidth = size().x();
        double x = 0.5 * (sceneWidth - text.length() * TS); // assume fixed font of size TS
        renderer.drawText(text, LABEL_COLOR, font, x, y);
    }

    private void drawArrowIfSelected(GameRenderer renderer, int option, double y, Font font) {
        if (selectedOption == option) {
            renderer.drawText("-", LABEL_COLOR, font, COL_ARROW, y);
            renderer.drawText(">", LABEL_COLOR, font, COL_ARROW + 3, y);
        }
    }

    //TODO use right sound
    private void playOptionSelectionChangedSound() {
        context.sound().playClipIfEnabled("option.selection_changed", 1);
    }

    //TODO use right sound
    private void playOptionValueChangedSound() {
        context.sound().playClipIfEnabled("option.value_changed", 1);
    }

    @Override
    public void handleInput(GameContext context) {

        // TODO simplify
        if (context.keyboard().pressedAndRegistered(context.joypad().down())) {
            playOptionSelectionChangedSound();
            selectNextOption();
            resetIdleTimer();
        }

        // TODO simplify
        else if (context.keyboard().pressedAndRegistered(context.joypad().up())) {
            playOptionSelectionChangedSound();
            selectPrevOption();
            resetIdleTimer();
        }

        // TODO simplify
        // Button "A" is right of "B": select forwards
        else if (context.keyboard().pressedAndRegistered(context.joypad().a())) {
            switch (selectedOption) {
                case OPTION_PAC_BOOSTER -> {
                    selectNextPacBoosterValue();
                    playOptionValueChangedSound();
                }
                case OPTION_DIFFICULTY -> {
                    selectNextDifficultyValue();
                    playOptionValueChangedSound();
                }
                case OPTION_MAZE_SELECTION -> {
                    selectNextMapCategoryValue();
                    playOptionValueChangedSound();
                }
                case OPTION_STARTING_LEVEL -> {
                    selectNextStartLevelValue();
                    playOptionValueChangedSound();
                }
                default -> {}
            }
            resetIdleTimer();
        }

        // Button "B" is left of "A": select backwards
        else if (context.keyboard().pressedAndRegistered(context.joypad().b())) {
            switch (selectedOption) {
                case OPTION_PAC_BOOSTER -> {
                    selectPrevPacBoosterValue();
                    playOptionValueChangedSound();
                }
                case OPTION_DIFFICULTY -> {
                    selectPrevDifficultyValue();
                    playOptionValueChangedSound();
                }
                case OPTION_MAZE_SELECTION -> {
                    selectPrevMapCategoryValue();
                    playOptionValueChangedSound();
                }
                case OPTION_STARTING_LEVEL -> {
                    selectPrevStartLevelValue();
                    playOptionValueChangedSound();
                }
                default -> {}
            }
            resetIdleTimer();
        }

        else if (context.keyboard().pressedAndRegistered(context.joypad().start())) {
            // start playing
            context.sound().stopAll();
            context.gameController().changeState(GameState.STARTING_GAME);
        }

        else {
            context.doFirstCalledGameAction(this);
        }
    }

    private void resetIdleTimer() {
        idleTicks = 0;
    }

    private void selectPrevOption() {
        selectedOption = selectedOption == 0 ? NUM_OPTIONS - 1 : selectedOption - 1;
    }

    private void selectNextOption() {
        selectedOption = (selectedOption < NUM_OPTIONS - 1) ? selectedOption + 1 : 0;
    }

    private void selectPrevStartLevelValue() {
        int current = tengenGame.startLevelNumber();
        int prev = (current == MIN_START_LEVEL) ? MAX_START_LEVEL : current - 1;
        tengenGame.setStartLevelNumber(prev);
    }

    private void selectNextStartLevelValue() {
        int current = tengenGame.startLevelNumber();
        int next = (current < MAX_START_LEVEL) ? current + 1 : MIN_START_LEVEL;
        tengenGame.setStartLevelNumber(next);
    }

    private void selectPrevMapCategoryValue() {
        MapCategory category = tengenGame.mapCategory();
        var values = MapCategory.values();
        int current = category.ordinal(), prev = (current == 0) ? values.length - 1 :  current - 1;
        tengenGame.setMapCategory(values[prev]);
    }

    private void selectNextMapCategoryValue() {
        MapCategory category = tengenGame.mapCategory();
        var values = MapCategory.values();
        int current = category.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        tengenGame.setMapCategory(values[next]);
    }

    private void selectPrevDifficultyValue() {
        Difficulty difficulty = tengenGame.difficulty();
        var values = Difficulty.values();
        int current = difficulty.ordinal(), prev = (current == 0) ? values.length - 1 : current - 1;
        tengenGame.setDifficulty(values[prev]);
    }

    private void selectNextDifficultyValue() {
        Difficulty difficulty = tengenGame.difficulty();
        var values = Difficulty.values();
        int current = difficulty.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        tengenGame.setDifficulty(values[next]);
    }

    private void selectPrevPacBoosterValue() {
        BoosterMode boosterMode = tengenGame.boosterMode();
        var values = BoosterMode.values();
        int current = boosterMode.ordinal(), prev = (current == 0) ? values.length - 1 : current - 1;
        tengenGame.setBoosterMode(values[prev]);
    }

    private void selectNextPacBoosterValue() {
        BoosterMode boosterMode = tengenGame.boosterMode();
        var values = BoosterMode.values();
        int current = boosterMode.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        tengenGame.setBoosterMode(values[next]);
    }
}