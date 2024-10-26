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
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameRenderer.TENGEN_BABY_BLUE;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameRenderer.TENGEN_YELLOW;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameSceneConfiguration.NES_SCREEN_HEIGHT;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameSceneConfiguration.NES_SCREEN_WIDTH;
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

    static final int MAX_STARTING_LEVEL = 7;

    private TengenMsPacManGame tengenGame;
    private int selectedOption;
    private long idleTicks;

    @Override
    public void bindActions() {
        bindAction(GameActions2D.TEST_CUT_SCENES,     alt(KeyCode.C));
        bindAction(GameActions2D.TEST_LEVELS_BONI,    alt(KeyCode.T));
        bindAction(GameActions2D.TEST_LEVELS_TEASERS, shift_alt(KeyCode.T));
    }

    @Override
    public void doInit() {
        context.setScoreVisible(false);
        selectedOption = OPTION_PAC_BOOSTER;
        tengenGame = (TengenMsPacManGame) context.game();
        tengenGame.setCanStartGame(true);
        resetIdleTimer();
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
        return new Vector2f(NES_SCREEN_WIDTH, NES_SCREEN_HEIGHT);
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
        renderer.drawText(pacBoosterText(tengenGame.pacBoosterMode()), VALUE_COLOR, font, COL_VALUE, y);

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
        renderer.drawText(String.valueOf(tengenGame.startingLevel()), VALUE_COLOR, font, COL_VALUE + TS, y);

        y += 3 * TS;
        drawCenteredText(renderer, "MOVE ARROW WITH CURSOR KEYS", font, y);
        y += TS;
        drawCenteredText(renderer, "CHOOSE OPTIONS WITH TAB", font, y);
        y += TS;
        drawCenteredText(renderer, "PRESS ENTER TO START GAME", font, y);

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
        context.sounds().playClipIfEnabled("option.selection_changed");
    }

    //TODO use right sound
    private void playOptionValueChangedSound() {
        context.sounds().playClipIfEnabled("option.value_changed");
    }

    @Override
    public void handleInput() {
        if (context.keyboard().pressed(KeyCode.DOWN)) {
            playOptionSelectionChangedSound();
            selectNextOption();
            resetIdleTimer();
        }
        else if (context.keyboard().pressed(KeyCode.UP)) {
            playOptionSelectionChangedSound();
            selectPrevOption();
            resetIdleTimer();
        }

        else if (context.keyboard().pressed(KeyCode.TAB)) {
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
                    selectNextMazeSelectionValue();
                    playOptionValueChangedSound();
                }
                case OPTION_STARTING_LEVEL -> {
                    selectNextStartingLevelValue();
                    playOptionValueChangedSound();
                }
                default -> {}
            }
            resetIdleTimer();
        }

        //TODO make into game action?
        else if (context.keyboard().pressed(KeyCode.ENTER)) { // start playing
            context.sounds().stopAll();
            context.gameController().changeState(GameState.STARTING_GAME);
        }

        else {
            context.doFirstCalledAction(this);
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

    private void selectNextStartingLevelValue() {
        int current = tengenGame.startingLevel();
        int next = (current < MAX_STARTING_LEVEL) ? current + 1 : 1;
        tengenGame.setStartingLevel(next);
    }

    private void selectNextMazeSelectionValue() {
        MapCategory category = tengenGame.mapCategory();
        var values = MapCategory.values();
        int current = category.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        tengenGame.setMapCategory(values[next]);
    }

    private void selectNextDifficultyValue() {
        Difficulty difficulty = tengenGame.difficulty();
        var values = Difficulty.values();
        int current = difficulty.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        tengenGame.setDifficulty(values[next]);
    }

    private void selectNextPacBoosterValue() {
        BoosterMode boosterMode = tengenGame.pacBoosterMode();
        var values = BoosterMode.values();
        int current = boosterMode.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        tengenGame.setPacBooster(values[next]);
    }
}