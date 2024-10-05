package de.amr.games.pacman.ui2d.scene.tengen;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.tengen.MsPacManTengenGame;
import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameAction2D;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import de.amr.games.pacman.ui2d.util.Keyboard;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui2d.scene.tengen.TengenGameWorldRenderer.YELLOWISH;

public class TengenSettingsScene extends GameScene2D {

    static final Color LABEL_COLOR = YELLOWISH;
    static final Color VALUE_COLOR = Color.WHITE;

    static final int NUM_SELECTIONS = 2;
    static final int SETTING_DIFFICULTY = 0;
    static final int SETTING_MAZE_SELECTION = 1;

    private int selectedSetting;

    @Override
    public void init() {
        selectedSetting = SETTING_DIFFICULTY;
    }

    @Override
    public void end() {
    }

    @Override
    public void update() {
    }

    @Override
    protected void drawSceneContent(GameWorldRenderer renderer) {
        MsPacManTengenGame tengenGame = (MsPacManTengenGame) context.game();
        int col1 = 2 * TS;
        int col2 = 19 * TS;
        Font font = renderer.scaledArcadeFont(TS);
        renderer.drawText("MS PAC-MAN OPTIONS", LABEL_COLOR, font, 6*TS, 6*TS);

        int y = 14 * TS;
        // setting 0
        drawArrowIfSelected(renderer, SETTING_DIFFICULTY, y);
        renderer.drawText("GAME DIFFICULTY:", LABEL_COLOR, font, col1, y);
        renderer.drawText(tengenGame.difficulty().name(), VALUE_COLOR, font, col2, y);

        y += 3*TS;
        // setting 1
        drawArrowIfSelected(renderer, SETTING_MAZE_SELECTION, y);
        renderer.drawText("MAZE SELECTION:", LABEL_COLOR, font, col1, y);
        renderer.drawText(tengenGame.mapCategory().name(), VALUE_COLOR, font, col2, y);

        font = renderer.scaledArcadeFont(7);
        renderer.drawText("MOVE ARROW WITH CURSOR KEYS", LABEL_COLOR, font, col1-TS, 30*TS);
        renderer.drawText("CHOOSE OPTIONS WITH ENTER", LABEL_COLOR, font, col1, 31*TS);
        renderer.drawText("PRESS SPACE TO START GAME", LABEL_COLOR, font, col1, 32*TS);
    }

    private void drawArrowIfSelected(GameWorldRenderer renderer, int setting, int y) {
        if (selectedSetting == setting) {
            Font font = renderer.scaledArcadeFont(TS);
            renderer.drawText(">", LABEL_COLOR, font, 0, y);
        }
    }

    @Override
    public boolean isCreditVisible() {
        return false;
    }

    private int fps(MsPacManTengenGame.Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 40;
            case NORMAL -> 60;
            case HARD -> 90;
            case CRAZY -> 120;
        };
    }

    @Override
    public void handleInput() {
        MsPacManTengenGame tengenGame = (MsPacManTengenGame) context.game();
        if (Keyboard.pressed(KeyCode.ENTER)) {
            switch (selectedSetting) {
                case SETTING_DIFFICULTY -> {
                    MsPacManTengenGame.Difficulty difficulty = tengenGame.difficulty();
                    int ord = difficulty.ordinal();
                    if (ord == MsPacManTengenGame.Difficulty.values().length - 1) {
                        tengenGame.setDifficulty(MsPacManTengenGame.Difficulty.values()[0]);
                    } else {
                        tengenGame.setDifficulty(MsPacManTengenGame.Difficulty.values()[ord + 1]);
                    }
                }
                case SETTING_MAZE_SELECTION -> {
                    MsPacManTengenGame.MapCategory category = tengenGame.mapCategory();
                    int ord = category.ordinal();
                    if (ord == MsPacManTengenGame.MapCategory.values().length - 1) {
                        tengenGame.setMapCategory(MsPacManTengenGame.MapCategory.values()[0]);
                    } else {
                        tengenGame.setMapCategory(MsPacManTengenGame.MapCategory.values()[ord + 1]);
                    }
                }
                default -> {}
            }
        }
        else if (Keyboard.pressed(KeyCode.SPACE)) {
            context.sounds().stopAll();
            context.game().insertCoin();
            //TODO when to change FPS? Only during hunting state?
            //context.gameClock().setTargetFrameRate(fps(tengenGame.difficulty()));
            context.gameController().changeState(GameState.READY);
        }
        else if (Keyboard.pressed(KeyCode.UP)) {
            selectedSetting = (selectedSetting > 0) ? selectedSetting - 1: NUM_SELECTIONS - 1;
        }
        else if (Keyboard.pressed(KeyCode.DOWN)) {
            selectedSetting = (selectedSetting < NUM_SELECTIONS - 1) ? selectedSetting + 1 : 0;
        }
        else {
            GameAction.calledAction(GameAction2D.TEST_LEVELS).ifPresent(action -> action.execute(context));
        }
    }
}
