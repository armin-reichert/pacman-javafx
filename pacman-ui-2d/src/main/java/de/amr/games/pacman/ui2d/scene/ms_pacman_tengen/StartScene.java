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
import de.amr.games.pacman.ui2d.GlobalGameActions2D;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui2d.scene.common.ScalingBehaviour;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameRenderer.TENGEN_BABY_BLUE;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameRenderer.TENGEN_YELLOW;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameSceneConfiguration.NES_SCREEN_HEIGHT;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameSceneConfiguration.NES_SCREEN_WIDTH;

/**
 * @author Armin Reichert
 */
public class StartScene extends GameScene2D {

    static final float UNIT = 7;

    static final int COL_ARROW = 3 * TS;
    static final int COL_LABEL = 5 * TS;
    static final int COL_COLON = 18 * TS;
    static final int COL_VALUE = 20 * TS;

    static final Color LABEL_COLOR = TENGEN_YELLOW;
    static final Color VALUE_COLOR = Color.WHITE;

    static final int OPTION_PLAYERS = 0;
    static final int OPTION_PAC_BOOSTER = 1;
    static final int OPTION_DIFFICULTY = 2;
    static final int OPTION_MAZE_SELECTION = 3;
    static final int OPTION_STARTING_LEVEL = 4;

    static final int NUM_OPTIONS = 5;

    private TengenMsPacManGame tengenGame;
    private int selectedOption;
    private long idleTicks;

    @Override
    public void init() {
        context.setScoreVisible(false);
        selectedOption = OPTION_PAC_BOOSTER;
        tengenGame = (TengenMsPacManGame) context.game();
        tengenGame.setCanStartGame(true);
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

    @Override
    public Vector2f size() {
        return new Vector2f(NES_SCREEN_WIDTH, NES_SCREEN_HEIGHT);
    }
    @Override
    protected void drawSceneContent(GameRenderer renderer) {
        renderer.scalingProperty().set(scaling());
        Font font = renderer.scaledArcadeFont(UNIT);

        renderer.ctx().setLineWidth(2);
        renderer.ctx().setStroke(Color.WHITE);
        renderer.ctx().strokeRect(0, 0, renderer.canvas().getWidth(), renderer.canvas().getHeight());

        float y = 4 * UNIT - 1;
        drawBabyBlueBar(renderer, y);

        y += 3 * UNIT + 2;
        renderer.drawText("MS PAC-MAN OPTIONS", LABEL_COLOR, font, COL_LABEL + 3 * UNIT, y);

        // Players (not implemented)
        y += 3 * UNIT;
        drawArrowIfSelected(renderer, OPTION_PLAYERS, y);
        renderer.drawText("TYPE", LABEL_COLOR, font, COL_LABEL, y);
        renderer.drawText(":", LABEL_COLOR, font, COL_LABEL + 4 * UNIT + 4, y);
        renderer.drawText("1 PLAYER", VALUE_COLOR, font, COL_LABEL + 6 * UNIT  , y);

        // Pac-Booster
        y += 3 * UNIT;
        drawArrowIfSelected(renderer, OPTION_PAC_BOOSTER, y);
        renderer.drawText("PAC BOOSTER", LABEL_COLOR, font, COL_LABEL, y);
        renderer.drawText(":", LABEL_COLOR, font, COL_COLON, y);
        renderer.drawText(pacBoosterText(tengenGame.pacBoosterMode()), VALUE_COLOR, font, COL_VALUE, y);

        // Game difficulty
        y += 3 * UNIT;
        drawArrowIfSelected(renderer, OPTION_DIFFICULTY, y);
        renderer.drawText("GAME DIFFICULTY", LABEL_COLOR, font, COL_LABEL, y);
        renderer.drawText(":", LABEL_COLOR, font, COL_COLON, y);
        renderer.drawText(tengenGame.difficulty().name(), VALUE_COLOR, font, COL_VALUE, y);

        // Maze (type) selection
        y += 3 * UNIT;
        drawArrowIfSelected(renderer, OPTION_MAZE_SELECTION, y);
        renderer.drawText("MAZE SELECTION", LABEL_COLOR, font, COL_LABEL, y);
        renderer.drawText(":", LABEL_COLOR, font, COL_COLON, y);
        renderer.drawText(tengenGame.mapCategory().name(), VALUE_COLOR, font, COL_VALUE, y);

        // Starting level number
        y += 3 * UNIT;
        drawArrowIfSelected(renderer, OPTION_STARTING_LEVEL, y);
        renderer.drawText("STARTING LEVEL", LABEL_COLOR, font, COL_LABEL, y);
        renderer.drawText(":", LABEL_COLOR, font, COL_COLON, y);
        renderer.drawText(String.valueOf(tengenGame.startingLevel()), VALUE_COLOR, font, COL_VALUE + UNIT, y);

        y += 2.5f * UNIT;
        drawCenteredText(renderer, size(), "MOVE ARROW WITH CURSOR KEYS", LABEL_COLOR, font, y);
        y += UNIT;
        drawCenteredText(renderer, size(), "CHOOSE OPTIONS WITH TAB", LABEL_COLOR, font, y);
        y += UNIT;
        drawCenteredText(renderer, size(), "PRESS ENTER TO START GAME", LABEL_COLOR, font, y);

        y += 3;
        drawBabyBlueBar(renderer, y);
    }

    private void drawBabyBlueBar(GameRenderer renderer, double y) {
        Canvas canvas = renderer.canvas();
        renderer.ctx().save();
        renderer.ctx().scale(scaling(), scaling());
        renderer.ctx().setFill(Color.WHITE);
        renderer.ctx().fillRect(0, y, canvas.getWidth(), UNIT);
        renderer.ctx().setFill(TENGEN_BABY_BLUE);
        renderer.ctx().fillRect(0, y + 1, canvas.getWidth(), UNIT - 2);
        renderer.ctx().restore();
    }

    private String pacBoosterText(BoosterMode boosterMode) {
        return switch (boosterMode) {
            case OFF -> "OFF";
            case ALWAYS_ON -> "ALWAYS ON";
            case ACTIVATED_USING_KEY -> "KEY A";
        };
    }

    private void drawCenteredText(GameRenderer renderer, Vector2f sceneSize, String text, Color color, Font font, double y) {
        double x = 0.5 * sceneSize.x() - 0.5 * text.length() * UNIT; // assume fixed font of size TS
        renderer.drawText(text, color, font, x, y);
    }

    private void drawArrowIfSelected(GameRenderer renderer, int option, float y) {
        if (selectedOption == option) {
            Font font = renderer.scaledArcadeFont(UNIT);
            renderer.drawText("-", LABEL_COLOR, font, COL_ARROW, y);
            renderer.drawText(">", LABEL_COLOR, font, COL_ARROW + 3, y);
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
        if (context.keyboard().pressed(KeyCode.DOWN)) {
            selectNextOption();
        }
        else if (context.keyboard().pressed(KeyCode.UP)) {
            selectPrevOption();
        }

        else if (context.keyboard().pressed(KeyCode.TAB)) {
            switch (selectedOption) {
                case OPTION_PAC_BOOSTER    -> selectNextPacBoosterValue();
                case OPTION_DIFFICULTY     -> selectNextDifficultyValue();
                case OPTION_MAZE_SELECTION -> selectNextMazeSelectionValue();
                case OPTION_STARTING_LEVEL -> selectNextStartingLevelValue(7); // max value
                default -> {}
            }
        }

        //TODO make into game action?
        else if (context.keyboard().pressed(KeyCode.ENTER)) { // start playing
            context.sounds().stopAll();
            context.gameController().changeState(GameState.READY);
        }

        else {
            context.doFirstCalledAction(
                GlobalGameActions2D.TEST_LEVELS_BONI,
                GlobalGameActions2D.TEST_LEVELS_TEASERS,
                GlobalGameActions2D.TEST_CUT_SCENES);
        }
    }

    private void resetIdleTimer() {
        idleTicks = 0;
    }

    private void selectPrevOption() {
        selectedOption = selectedOption == 0 ? NUM_OPTIONS - 1 : selectedOption - 1;
        playChangeOptionSound();
        resetIdleTimer();
    }

    private void selectNextOption() {
        selectedOption = (selectedOption < NUM_OPTIONS - 1) ? selectedOption + 1 : 0;
        playChangeOptionSound();
        resetIdleTimer();
    }

    private void selectNextStartingLevelValue(int maxValue) {
        int current = tengenGame.startingLevel();
        int next = (current < maxValue) ? current + 1 : 1;
        tengenGame.setStartingLevel(next);
        playChangeOptionValueSound();
        resetIdleTimer();
    }

    private void selectNextMazeSelectionValue() {
        MapCategory category = tengenGame.mapCategory();
        var values = MapCategory.values();
        int current = category.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        tengenGame.setMapCategory(values[next]);
        playChangeOptionValueSound();
        resetIdleTimer();
    }

    private void selectNextDifficultyValue() {
        Difficulty difficulty = tengenGame.difficulty();
        var values = Difficulty.values();
        int current = difficulty.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        tengenGame.setDifficulty(values[next]);
        playChangeOptionValueSound();
        resetIdleTimer();
    }

    private void selectNextPacBoosterValue() {
        BoosterMode boosterMode = tengenGame.pacBoosterMode();
        var values = BoosterMode.values();
        int current = boosterMode.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        tengenGame.setPacBooster(values[next]);
        playChangeOptionValueSound();
        resetIdleTimer();
    }
}