/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.controller.GamePlayState;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.Difficulty;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.tengen.ms_pacman.model.PacBooster;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_SceneRenderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.uilib.rendering.HUDRenderer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Actions.ACTION_START_PLAYING;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Actions.ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Properties.PROPERTY_JOYPAD_BINDINGS_DISPLAYED;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesColor;
import static de.amr.pacmanfx.ui.action.CommonGameActions.*;
import static de.amr.pacmanfx.ui.input.Keyboard.alt;

/**
 * Options scene for Ms. Pac-Man Tengen.
 *
 * <p></p>The highscore is cleared if player type (1 player, 2 players etc.), map category or difficulty are
 * changed, see <a href="https://github.com/RussianManSMWC/Ms.-Pac-Man-NES-Tengen-Disassembly/blob/main/MsPacManTENGENDis.asm:9545">disassembly</a>.
 */
public class TengenMsPacMan_OptionsScene extends GameScene2D {

    private static final int COL_ARROW = 2 * TS;
    private static final int COL_LABEL = 4 * TS;
    private static final int COL_COLON = 19 * TS;
    private static final int COL_VALUE = 21  * TS;

    private static final Color NES_YELLOW = nesColor(0x28);
    private static final Color NES_WHITE = nesColor(0x20);

    private static final byte OPTION_PLAYERS = 0;
    private static final byte OPTION_PAC_BOOSTER = 1;
    private static final byte OPTION_DIFFICULTY = 2;
    private static final byte OPTION_MAZE_SELECTION = 3;
    private static final byte OPTION_STARTING_LEVEL = 4;

    private static final byte NUM_OPTIONS = 5;

    private static final byte MIN_START_LEVEL = 1;
    private static final byte MAX_START_LEVEL = 32;

    private static final int INITIAL_DELAY = 20; //TODO verify
    private static final int IDLE_TIMEOUT = 1530; // 25,5 sec TODO verify

    private final IntegerProperty selectedOption = new SimpleIntegerProperty() {
        @Override
        protected void invalidated() {
            ui.soundManager().play("audio.option.selection_changed");
            idleTicks = 0;
        }
    };

    private final GameAction actionSelectNextJoypadBinding = new GameAction("SELECT_NEXT_JOYPAD_BINDING") {
        @Override
        public void execute(GameUI ui) {
            ui.joypad().selectNextBinding(actionBindingsManager);
        }
    };

    private TengenMsPacMan_SceneRenderer sceneRenderer;

    private int idleTicks;
    private int initialDelay;

    public TengenMsPacMan_OptionsScene(GameUI ui) {
        super(ui);
    }

    @Override
    protected void createRenderers(Canvas canvas) {
        super.createRenderers(canvas);
        sceneRenderer = configureRenderer(new TengenMsPacMan_SceneRenderer(canvas, ui.currentConfig()));
    }

    @Override
    protected HUDRenderer hudRenderer() {
        return null;
    }

    @Override
    public void doInit() {
        context().game().hud().all(false);

        var tengenActionBindings = ui.<TengenMsPacMan_UIConfig>currentConfig().tengenActionBindings();
        actionBindingsManager.setKeyCombination(actionSelectNextJoypadBinding, alt(KeyCode.J)); //TODO
        actionBindingsManager.useBindings(ACTION_START_PLAYING, tengenActionBindings);
        actionBindingsManager.useBindings(ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY, tengenActionBindings);
        actionBindingsManager.useBindings(ACTION_TEST_CUT_SCENES, ui.actionBindings());
        actionBindingsManager.useBindings(ACTION_TEST_LEVELS_SHORT, ui.actionBindings());
        actionBindingsManager.useBindings(ACTION_TEST_LEVELS_MEDIUM, ui.actionBindings());
        ui.joypad().setBindings(actionBindingsManager);

        selectedOption.set(OPTION_PAC_BOOSTER);
        theGame().setCanStartNewGame(true);

        idleTicks = 0;
        initialDelay = INITIAL_DELAY;
    }

    @Override
    protected void doEnd() {
        ui.joypad().removeBindings(actionBindingsManager);
    }

    @Override
    public void update() {
        if (initialDelay > 0) {
            --initialDelay;
            return;
        }
        if (idleTicks < IDLE_TIMEOUT) {
            idleTicks += 1;
        } else {
            context().gameController().changeGameState(GamePlayState.INTRO);
        }
    }

    @Override
    public Vector2i sizeInPx() { return NES_SIZE_PX; }

    private TengenMsPacMan_GameModel theGame() { return context().game(); }
    
    private void optionValueChanged() {
        ui.soundManager().play("audio.option.value_changed");
        idleTicks = 0;
    }

    private int selectedOption() {
        return selectedOption.get();
    }

    @Override
    public void handleKeyboardInput() {
        if (ui.joypad().isButtonPressed(JoypadButton.DOWN)) {
            selectedOption.set(selectedOption() + 1 < NUM_OPTIONS ? selectedOption() + 1 : 0);
        }
        else if (ui.joypad().isButtonPressed(JoypadButton.UP)) {
            selectedOption.set(selectedOption() == 0 ? NUM_OPTIONS - 1 : selectedOption() - 1);
        }
        // Button "A" on the joypad is located right of "B": select next value
        else if (ui.joypad().isButtonPressed(JoypadButton.A) || ui.keyboard().isPressed(KeyCode.RIGHT)) {
            switch (selectedOption()) {
                case OPTION_PAC_BOOSTER    -> setNextPacBoosterValue();
                case OPTION_DIFFICULTY     -> setNextDifficultyValue();
                case OPTION_MAZE_SELECTION -> setNextMapCategoryValue();
                case OPTION_STARTING_LEVEL -> setNextStartLevelValue();
            }
        }
        // Button "B" is left of "A": select previous value
        else if (ui.joypad().isButtonPressed(JoypadButton.B) || ui.keyboard().isPressed(KeyCode.LEFT)) {
            switch (selectedOption()) {
                case OPTION_PAC_BOOSTER    -> setPrevPacBoosterValue();
                case OPTION_DIFFICULTY     -> setPrevDifficultyValue();
                case OPTION_MAZE_SELECTION -> setPrevMapCategoryValue();
                case OPTION_STARTING_LEVEL -> setPrevStartLevelValue();
            }
        }
        else super.handleKeyboardInput();
    }

    private void setPrevStartLevelValue() {
        int current = theGame().startLevelNumber();
        int prev = (current == MIN_START_LEVEL) ? MAX_START_LEVEL : current - 1;
        theGame().setStartLevelNumber(prev);
        optionValueChanged();
    }

    private void setNextStartLevelValue() {
        int current = theGame().startLevelNumber();
        int next = (current < MAX_START_LEVEL) ? current + 1 : MIN_START_LEVEL;
        theGame().setStartLevelNumber(next);
        optionValueChanged();
    }

    private void setPrevMapCategoryValue() {
        MapCategory category = theGame().mapCategory();
        var values = MapCategory.values();
        int current = category.ordinal(), prev = (current == 0) ? values.length - 1 :  current - 1;
        theGame().setMapCategory(values[prev]);
        theGame().scoreManager().saveHighScore();
        optionValueChanged();
    }

    private void setNextMapCategoryValue() {
        MapCategory category = theGame().mapCategory();
        var values = MapCategory.values();
        int current = category.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        theGame().setMapCategory(values[next]);
        theGame().scoreManager().saveHighScore();
        optionValueChanged();
    }

    private void setPrevDifficultyValue() {
        Difficulty difficulty = theGame().difficulty();
        var values = Difficulty.values();
        int current = difficulty.ordinal(), prev = (current == 0) ? values.length - 1 : current - 1;
        theGame().setDifficulty(values[prev]);
        theGame().scoreManager().saveHighScore();
        optionValueChanged();
    }

    private void setNextDifficultyValue() {
        Difficulty difficulty = theGame().difficulty();
        var values = Difficulty.values();
        int current = difficulty.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        theGame().setDifficulty(values[next]);
        theGame().scoreManager().saveHighScore();
        optionValueChanged();
    }

    private void setPrevPacBoosterValue() {
        PacBooster pacBooster = theGame().pacBooster();
        var values = PacBooster.values();
        int current = pacBooster.ordinal(), prev = (current == 0) ? values.length - 1 : current - 1;
        theGame().setPacBooster(values[prev]);
        optionValueChanged();
    }

    private void setNextPacBoosterValue() {
        PacBooster pacBooster = theGame().pacBooster();
        var values = PacBooster.values();
        int current = pacBooster.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        theGame().setPacBooster(values[next]);
        optionValueChanged();
    }

    // Drawing

    @Override
    public void drawSceneContent() {
        if (initialDelay > 0) {
            return;
        }

        sceneRenderer.ctx().setFont(sceneRenderer.arcadeFontTS());
        if (PROPERTY_JOYPAD_BINDINGS_DISPLAYED.get()) {
            sceneRenderer.drawJoypadKeyBinding(ui.joypad().currentKeyBinding());
        }

        sceneRenderer.drawHorizontalBar(nesColor(0x20), nesColor(0x21), sizeInPx().x(), TS, 20);

        float y = 48;
        sceneRenderer.fillText("MS PAC-MAN OPTIONS", NES_YELLOW, COL_LABEL + 3 * TS, 48);

        y += TS(3);
        // Players (not implemented)
        drawMarkerIfSelected(OPTION_PLAYERS, y, sceneRenderer.arcadeFontTS());
        sceneRenderer.fillText("TYPE", NES_YELLOW, COL_LABEL, y);
        sceneRenderer.fillText(":", NES_YELLOW, COL_LABEL + 4 * TS + 4, y);
        // grey out
        sceneRenderer.fillText("1 PLAYER", nesColor(0x10), COL_LABEL + 6 * TS, y);

        y += TS(3);
        // Pac-Booster
        drawMarkerIfSelected(OPTION_PAC_BOOSTER, y, sceneRenderer.arcadeFontTS());
        sceneRenderer.fillText("PAC BOOSTER", NES_YELLOW, COL_LABEL, y);
        sceneRenderer.fillText(":", NES_YELLOW, COL_COLON, y);
        String pacBoosterText = switch (theGame().pacBooster()) {
            case OFF -> "OFF";
            case ALWAYS_ON -> "ALWAYS ON";
            case USE_A_OR_B -> "USE A OR B";
        };
        sceneRenderer.fillText(pacBoosterText, NES_WHITE, COL_VALUE, y);

        y += TS(3);
        // Game difficulty
        drawMarkerIfSelected(OPTION_DIFFICULTY, y, sceneRenderer.arcadeFontTS());
        sceneRenderer.fillText("GAME DIFFICULTY", NES_YELLOW, COL_LABEL, y);
        sceneRenderer.fillText(":", NES_YELLOW, COL_COLON, y);
        sceneRenderer.fillText(theGame().difficulty().name(), NES_WHITE, COL_VALUE, y);

        y += TS(3);
        // Maze (type) selection
        drawMarkerIfSelected(OPTION_MAZE_SELECTION, y, sceneRenderer.arcadeFontTS());
        sceneRenderer.fillText("MAZE SELECTION", NES_YELLOW, COL_LABEL, y);
        sceneRenderer.fillText(":", NES_YELLOW, COL_COLON, y);
        sceneRenderer.fillText(theGame().mapCategory().name(), NES_WHITE, COL_VALUE, y);

        y += TS(3);
        // Starting level number
        drawMarkerIfSelected(OPTION_STARTING_LEVEL, y, sceneRenderer.arcadeFontTS());
        sceneRenderer.fillText("STARTING LEVEL", NES_YELLOW, COL_LABEL, y);
        sceneRenderer.fillText(":", NES_YELLOW, COL_COLON, y);
        sceneRenderer.fillText(String.valueOf(theGame().startLevelNumber()), NES_WHITE, COL_VALUE, y);
        final int numContinues = theGame().numContinues();
        if (numContinues < 4) {
            var spriteSheet = (TengenMsPacMan_SpriteSheet) ui.currentConfig().spriteSheet();
            RectShort continuesSprite = spriteSheet.sprite(switch (numContinues) {
                case 0 -> SpriteID.CONTINUES_0;
                case 1 -> SpriteID.CONTINUES_1;
                case 2 -> SpriteID.CONTINUES_2;
                case 3 -> SpriteID.CONTINUES_3;
                default -> throw new IllegalArgumentException("Illegal number of continues: " + numContinues);
            });
            sceneRenderer.drawSprite(continuesSprite, COL_VALUE + 3 * TS, y - 8, true);
        }

        y += TS(3);
        sceneRenderer.fillText("MOVE ARROW WITH JOYPAD",      NES_YELLOW, TS(4), y);

        y += TS(1);
        sceneRenderer.fillText("CHOOSE OPTIONS WITH A AND B", NES_YELLOW, TS(2), y);

        y += TS(1);
        sceneRenderer.fillText("PRESS START TO START GAME",   NES_YELLOW, TS(3), y);

        sceneRenderer.drawHorizontalBar(nesColor(0x20), nesColor(0x21), sizeInPx().x(), TS, 212);
    }

    private void drawMarkerIfSelected(int optionIndex, double y, Font font) {
        if (selectedOption() == optionIndex) {
            sceneRenderer.ctx().setFill(NES_YELLOW);
            sceneRenderer.ctx().fillRect(scaled(COL_ARROW + 2.25), scaled(y - 4.5), scaled(7.5), scaled(1.75));
            sceneRenderer.fillText(">", NES_YELLOW, font, COL_ARROW + 3, y);
        }
    }
}