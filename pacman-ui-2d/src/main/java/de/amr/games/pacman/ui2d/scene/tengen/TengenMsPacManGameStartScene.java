/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.tengen;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.tengen.MsPacManTengenGame;
import de.amr.games.pacman.ui2d.GameAction2D;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui2d.scene.tengen.TengenMsPacManGameRenderer.TENGEN_BABY_BLUE;
import static de.amr.games.pacman.ui2d.scene.tengen.TengenMsPacManGameRenderer.TENGEN_YELLOW;

/**
 * @author Armin Reichert
 */
public class TengenMsPacManGameStartScene extends GameScene2D {

    static final int COL_ARROW = 0;
    static final int COL_LABEL = 2 * TS;
    static final int COL_COLON = 17 * TS;
    static final int COL_VALUE = 19 * TS;

    static final Color LABEL_COLOR = TENGEN_YELLOW;
    static final Color VALUE_COLOR = Color.WHITE;

    static final int SETTING_PLAYERS        = 0;
    static final int OPTION_PAC_BOOSTER = 1;
    static final int OPTION_DIFFICULTY = 2;
    static final int OPTION_MAZE_SELECTION = 3;
    static final int OPTION_STARTING_LEVEL = 4;

    static final int NUM_SELECTIONS = 5;

    private int option;
    private MsPacManTengenGame tengenGame;

    private long idleTicks;

    @Override
    public void init() {
        context.setScoreVisible(false);
        option = OPTION_PAC_BOOSTER;
        tengenGame = (MsPacManTengenGame) context.game();
        resetIdleTimer();
    }

    @Override
    public void end() {
    }

    @Override
    public void update() {
        if (idleTicks == 15*60) {
            context.gameController().changeState(GameState.INTRO);
            return;
        }
        idleTicks += 1;
    }

    private void drawBabyBlueBar(GameWorldRenderer renderer, double y) {
        Canvas canvas = renderer.canvas();
        renderer.ctx().save();
        renderer.ctx().scale(scaling(), scaling());
        renderer.ctx().setFill(Color.WHITE);
        renderer.ctx().fillRect(0, y, canvas.getWidth(), 8);
        renderer.ctx().setFill(TENGEN_BABY_BLUE);
        renderer.ctx().fillRect(0, y + 1, canvas.getWidth(), 6);
        renderer.ctx().restore();
    }

    @Override
    protected void drawLevelCounter(GameWorldRenderer renderer, Vector2i worldSize) {
        // suppress level counter
    }

    @Override
    protected void drawSceneContent(GameWorldRenderer renderer) {
        Font font = renderer.scaledArcadeFont(TS);

        int y = 7 * TS;
        drawBabyBlueBar(renderer, y);

        y += 3 * TS + 2;
        renderer.drawText("MS PAC-MAN OPTIONS", LABEL_COLOR, font, 6 * TS, y);

        // Players (not implemented)
        y += 3 * TS;
        drawArrowIfSelected(renderer, SETTING_PLAYERS, COL_ARROW, y);
        renderer.drawText("TYPE", LABEL_COLOR, font, COL_LABEL, y);
        renderer.drawText(":", LABEL_COLOR, font, COL_LABEL + 4 * TS + 4, y);
        renderer.drawText("1 PLAYER", VALUE_COLOR, font, COL_LABEL + 6 * TS  , y);

        // Pac-Booster
        y += 3 * TS;
        drawArrowIfSelected(renderer, OPTION_PAC_BOOSTER, COL_ARROW, y);
        renderer.drawText("PAC BOOSTER", LABEL_COLOR, font, COL_LABEL, y);
        renderer.drawText(":", LABEL_COLOR, font, COL_COLON, y);
        renderer.drawText(pacBoosterText(tengenGame.pacBooster()), VALUE_COLOR, font, COL_VALUE, y);

        // Game difficulty
        y += 3 * TS;
        drawArrowIfSelected(renderer, OPTION_DIFFICULTY, COL_ARROW, y);
        renderer.drawText("GAME DIFFICULTY", LABEL_COLOR, font, COL_LABEL, y);
        renderer.drawText(":", LABEL_COLOR, font, COL_COLON, y);
        renderer.drawText(tengenGame.difficulty().name(), VALUE_COLOR, font, COL_VALUE, y);

        // Maze (type) selection
        y += 3 * TS;
        drawArrowIfSelected(renderer, OPTION_MAZE_SELECTION, COL_ARROW, y);
        renderer.drawText("MAZE SELECTION", LABEL_COLOR, font, COL_LABEL, y);
        renderer.drawText(":", LABEL_COLOR, font, COL_COLON, y);
        renderer.drawText(tengenGame.mapCategory().name(), VALUE_COLOR, font, COL_VALUE, y);

        // Starting level number
        y += 3 * TS;
        drawArrowIfSelected(renderer, OPTION_STARTING_LEVEL, COL_ARROW, y);
        renderer.drawText("STARTING LEVEL", LABEL_COLOR, font, COL_LABEL, y);
        renderer.drawText(":", LABEL_COLOR, font, COL_COLON, y);
        renderer.drawText(String.valueOf(tengenGame.startingLevel()), VALUE_COLOR, font, COL_VALUE + TS, y);

        y += 3 * TS + 3;
        drawCenteredText(renderer, "MOVE ARROW WITH CURSOR KEYS", LABEL_COLOR, font, y);
        y += TS + 1;
        drawCenteredText(renderer, "CHOOSE OPTIONS WITH TAB", LABEL_COLOR, font, y);
        y += TS + 1;
        drawCenteredText(renderer, "PRESS ENTER TO START GAME", LABEL_COLOR, font, y);

        y += TS - 3;
        drawBabyBlueBar(renderer, y);
    }

    private String pacBoosterText(MsPacManTengenGame.PacBooster pacBooster) {
        return switch (pacBooster) {
            case OFF -> "OFF";
            case ALWAYS_ON -> "ALWAYS ON";
            case TOGGLE_USING_KEY -> "KEY A";
        };
    }

    private void drawCenteredText(GameWorldRenderer renderer, String text, Color color, Font font, double y) {
        renderer.drawText(text, color, font, 0.5 * TS * (28 - text.length()), y);
    }

    private void drawArrowIfSelected(GameWorldRenderer renderer, int setting, int x, int y) {
        if (option == setting) {
            Font font = renderer.scaledArcadeFont(TS);
            renderer.drawText("-", LABEL_COLOR, font, x, y);
            renderer.drawText(">", LABEL_COLOR, font, x + 3, y);
        }
    }

    private void playChangeOptionSound() {
        //TODO use right sound
    }

    private void playChangeOptionValueSound() {
        //TODO use right sound
        context.sounds().playBonusEatenSound();
    }

    @Override
    public void handleInput() {
        //TODO Put all these cases into Tengen-specific game actions?

        if (context.keyboard().pressed(KeyCode.DOWN)) {
            selectNextOption();
        }

        else if (context.keyboard().pressed(KeyCode.UP)) {
            selectPrevOption();
        }

        else if (context.keyboard().pressed(KeyCode.TAB)) {
            switch (option) {
                case OPTION_PAC_BOOSTER    -> selectNextPacBoosterValue();
                case OPTION_DIFFICULTY     -> selectNextDifficultyValue();
                case OPTION_MAZE_SELECTION -> selectNextMazeSelectionValue();
                case OPTION_STARTING_LEVEL -> selectNextStartingLevelValue();
                default -> {}
            }
        }

        else if (context.keyboard().pressed(KeyCode.ENTER)) {
            // get ready to play
            context.sounds().stopAll();
            context.game().insertCoin(); //TODO check this
            context.gameController().changeState(GameState.READY);
        }

        else {
            context.execFirstCalledAction(
                GameAction2D.TEST_LEVELS_BONI,
                GameAction2D.TEST_LEVELS_TEASERS,
                GameAction2D.TEST_CUT_SCENES);
        }
    }

    private void resetIdleTimer() {
        idleTicks = 0;
    }

    private void selectPrevOption() {
        option = option == 0 ? NUM_SELECTIONS - 1 : option - 1;
        playChangeOptionSound();
        resetIdleTimer();
    }

    private void selectNextOption() {
        option = (option < NUM_SELECTIONS - 1) ? option + 1 : 0;
        playChangeOptionSound();
        resetIdleTimer();
    }

    private void selectNextStartingLevelValue() {
        int current = tengenGame.startingLevel();
        int next = (current < 7) ? current + 1 : 1;
        tengenGame.setStartingLevel(next);
        playChangeOptionValueSound();
        resetIdleTimer();
    }

    private void selectNextMazeSelectionValue() {
        MsPacManTengenGame.MapCategory category = tengenGame.mapCategory();
        var values = MsPacManTengenGame.MapCategory.values();
        int current = category.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        tengenGame.setMapCategory(values[next]);
        playChangeOptionValueSound();
        resetIdleTimer();
    }

    private void selectNextDifficultyValue() {
        MsPacManTengenGame.Difficulty difficulty = tengenGame.difficulty();
        var values = MsPacManTengenGame.Difficulty.values();
        int current = difficulty.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        tengenGame.setDifficulty(values[next]);
        playChangeOptionValueSound();
        resetIdleTimer();
    }

    private void selectNextPacBoosterValue() {
        MsPacManTengenGame.PacBooster pacBooster = tengenGame.pacBooster();
        var values = MsPacManTengenGame.PacBooster.values();
        int current = pacBooster.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        tengenGame.setPacBooster(values[next]);
        playChangeOptionValueSound();
        resetIdleTimer();
    }
}