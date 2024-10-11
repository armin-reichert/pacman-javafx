/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.tengen;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.tengen.MsPacManTengenGame;
import de.amr.games.pacman.ui2d.GameAction2D;
import de.amr.games.pacman.ui2d.GameAssets2D;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.TS;

/**
 * @author Armin Reichert
 */
public class TengenStartScene extends GameScene2D {

    static final int COL_ARROW = 0;
    static final int COL_LABEL = 2 * TS;
    static final int COL_COLON = 17 * TS;
    static final int COL_VALUE = 19 * TS;

    static final Color LABEL_COLOR = GameAssets2D.TENGEN_YELLOW;
    static final Color VALUE_COLOR = Color.WHITE;
    static final Color BABY_BLUE = Color.rgb(59, 190, 255);

    static final int SETTING_PLAYERS        = 0;
    static final int SETTING_PAC_BOOSTER    = 1;
    static final int SETTING_DIFFICULTY     = 2;
    static final int SETTING_MAZE_SELECTION = 3;
    static final int SETTING_STARTING_LEVEL = 4;

    static final int NUM_SELECTIONS = 5;

    private int selection;
    private MsPacManTengenGame tengenGame;

    @Override
    public void init() {
        context.setScoreVisible(false);
        selection = SETTING_PAC_BOOSTER;
        tengenGame = (MsPacManTengenGame) context.game();
    }

    @Override
    public void end() {
    }

    @Override
    public void update() {
    }

    private void drawBabyBlueBar(GameWorldRenderer renderer, double y) {
        Canvas canvas = renderer.canvas();
        renderer.ctx().save();
        renderer.ctx().scale(scaling(), scaling());
        renderer.ctx().setFill(Color.WHITE);
        renderer.ctx().fillRect(0, y, canvas.getWidth(), 8);
        renderer.ctx().setFill(BABY_BLUE);
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

        y += 3 * TS;
        renderer.drawText("MS PAC-MAN OPTIONS", LABEL_COLOR, font, 6 * TS, y);

        // Players (not implemented)
        y += 3 * TS;
        drawArrowIfSelected(renderer, SETTING_PLAYERS, COL_ARROW, y);
        renderer.drawText("TYPE", LABEL_COLOR, font, COL_LABEL, y);
        renderer.drawText(":", LABEL_COLOR, font, COL_LABEL + 4 * TS + 4, y);
        renderer.drawText("1 PLAYER", VALUE_COLOR, font, COL_LABEL + 6 * TS  , y);

        // Pac-Booster
        y += 3 * TS;
        drawArrowIfSelected(renderer, SETTING_PAC_BOOSTER, COL_ARROW, y);
        renderer.drawText("PAC BOOSTER", LABEL_COLOR, font, COL_LABEL, y);
        renderer.drawText(":", LABEL_COLOR, font, COL_COLON, y);
        renderer.drawText(pacBoosterText(tengenGame.pacBooster()), VALUE_COLOR, font, COL_VALUE, y);

        // Game difficulty
        y += 3 * TS;
        drawArrowIfSelected(renderer, SETTING_DIFFICULTY, COL_ARROW, y);
        renderer.drawText("GAME DIFFICULTY", LABEL_COLOR, font, COL_LABEL, y);
        renderer.drawText(":", LABEL_COLOR, font, COL_COLON, y);
        renderer.drawText(tengenGame.difficulty().name(), VALUE_COLOR, font, COL_VALUE, y);

        // Maze (type) selection
        y += 3 * TS;
        drawArrowIfSelected(renderer, SETTING_MAZE_SELECTION, COL_ARROW, y);
        renderer.drawText("MAZE SELECTION", LABEL_COLOR, font, COL_LABEL, y);
        renderer.drawText(":", LABEL_COLOR, font, COL_COLON, y);
        renderer.drawText(tengenGame.mapCategory().name(), VALUE_COLOR, font, COL_VALUE, y);

        // Starting level number
        y += 3 * TS;
        drawArrowIfSelected(renderer, SETTING_STARTING_LEVEL, COL_ARROW, y);
        renderer.drawText("STARTING LEVEL", LABEL_COLOR, font, COL_LABEL, y);
        renderer.drawText(":", LABEL_COLOR, font, COL_COLON, y);
        renderer.drawText(String.valueOf(tengenGame.startingLevel()), VALUE_COLOR, font, COL_VALUE + TS, y);

        y += 3 * TS;
        drawCenteredText(renderer, "MOVE ARROW WITH CURSOR KEYS", LABEL_COLOR, font, y);
        y += TS + 1;
        drawCenteredText(renderer, "CHOOSE OPTIONS WITH TAB", LABEL_COLOR, font, y);
        y += TS + 1;
        drawCenteredText(renderer, "PRESS ENTER TO START GAME", LABEL_COLOR, font, y);

        y += TS;
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
        if (selection == setting) {
            Font font = renderer.scaledArcadeFont(TS);
            renderer.drawText("-", LABEL_COLOR, font, x, y);
            renderer.drawText(">", LABEL_COLOR, font, x + 3, y);
        }
    }

    private int fps(MsPacManTengenGame.Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 40;
            case NORMAL -> 60;
            case HARD -> 90;
            case CRAZY -> 120;
        };
    }

    private void playChangeSelectionSound() {
        //TODO use right sound
    }

    private void playChangeValueSound() {
        context.sounds().playBonusEatenSound(); //TOD use right sound
    }

    @Override
    public void handleInput() {
        if (context.keyboard().pressed(KeyCode.DOWN)) {
            selection = (selection < NUM_SELECTIONS - 1) ? selection + 1 : 0;
            playChangeSelectionSound();
        }
        else if (context.keyboard().pressed(KeyCode.UP)) {
            selection = selection == 0 ? NUM_SELECTIONS - 1 : selection - 1;
            playChangeSelectionSound();
        }
        else if (context.keyboard().pressed(KeyCode.TAB)) {
            switch (selection) {
                case SETTING_PAC_BOOSTER -> {
                    MsPacManTengenGame.PacBooster pacBooster = tengenGame.pacBooster();
                    int ord = pacBooster.ordinal();
                    if (ord == MsPacManTengenGame.PacBooster.values().length - 1) {
                        tengenGame.setPacBooster(MsPacManTengenGame.PacBooster.values()[0]);
                    } else {
                        tengenGame.setPacBooster(MsPacManTengenGame.PacBooster.values()[ord + 1]);
                    }
                    playChangeValueSound();
                }
                case SETTING_DIFFICULTY -> {
                    MsPacManTengenGame.Difficulty difficulty = tengenGame.difficulty();
                    int ord = difficulty.ordinal();
                    if (ord == MsPacManTengenGame.Difficulty.values().length - 1) {
                        tengenGame.setDifficulty(MsPacManTengenGame.Difficulty.values()[0]);
                    } else {
                        tengenGame.setDifficulty(MsPacManTengenGame.Difficulty.values()[ord + 1]);
                    }
                    playChangeValueSound();
                }
                case SETTING_MAZE_SELECTION -> {
                    MsPacManTengenGame.MapCategory category = tengenGame.mapCategory();
                    int ord = category.ordinal();
                    if (ord == MsPacManTengenGame.MapCategory.values().length - 1) {
                        tengenGame.setMapCategory(MsPacManTengenGame.MapCategory.values()[0]);
                    } else {
                        tengenGame.setMapCategory(MsPacManTengenGame.MapCategory.values()[ord + 1]);
                    }
                    playChangeValueSound();
                }
                case SETTING_STARTING_LEVEL -> {
                    if (tengenGame.startingLevel() < 7) {
                        tengenGame.setStartingLevel(tengenGame.startingLevel() + 1);
                    } else {
                        tengenGame.setStartingLevel(1);
                    }
                    playChangeValueSound();
                }
                default -> {}
            }
        }
        else if (context.keyboard().pressed(KeyCode.ENTER)) {
            context.sounds().stopAll();
            context.game().insertCoin();
            //TODO when to change FPS? Only during hunting state?
            //context.gameClock().setTargetFrameRate(fps(tengenGame.difficulty()));
            context.gameController().changeState(GameState.READY);
        }
        else {
            context.execFirstCalledAction(
                GameAction2D.TEST_LEVELS_AND_BONUSES,
                GameAction2D.TEST_LEVELS_TEASERS,
                GameAction2D.TEST_CUT_SCENES);
        }
    }
}