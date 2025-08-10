/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.Difficulty;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.tengen.ms_pacman.model.PacBooster;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_GameRenderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.GameAction;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesPaletteColor;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.*;
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

    private static final Color NES_YELLOW = nesPaletteColor(0x28);
    private static final Color NES_WHITE = nesPaletteColor(0x20);

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
            ui.theSound().play("audio.option.selection_changed");
            idleTicks = 0;
        }
    };

    private final GameAction actionSelectNextJoypadBinding = new GameAction("SELECT_NEXT_JOYPAD_BINDING") {
        @Override
        public void execute(GameUI ui) {
            ui.theJoypad().selectNextBinding(actionBindings);
        }
    };

    private int idleTicks;
    private int initialDelay;

    public TengenMsPacMan_OptionsScene(GameUI ui) {
        super(ui);
    }

    @Override
    public void doInit() {
        gameContext().theGame().theHUD().all(false);

        var config = ui.<TengenMsPacMan_UIConfig>theConfiguration();
        actionBindings.use(config.ACTION_START_PLAYING, config.actionBindings);
        actionBindings.use(config.ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAYED, config.actionBindings);
        actionBindings.bind(actionSelectNextJoypadBinding, alt(KeyCode.J));
        actionBindings.use(ACTION_TEST_CUT_SCENES, ui.actionBindings());
        actionBindings.use(ACTION_TEST_LEVELS_BONI, ui.actionBindings());
        actionBindings.use(ACTION_TEST_LEVELS_TEASERS, ui.actionBindings());
        ui.theJoypad().setBindings(actionBindings);

        selectedOption.set(OPTION_PAC_BOOSTER);
        theGame().setCanStartNewGame(true);

        idleTicks = 0;
        initialDelay = INITIAL_DELAY;
    }

    @Override
    protected void doEnd() {
        ui.theJoypad().removeBindings(actionBindings);
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
            gameContext().theGameController().changeGameState(GameState.INTRO);
        }
    }

    @Override
    public Vector2f sizeInPx() { return NES_SIZE_PX; }

    private TengenMsPacMan_GameModel theGame() { return gameContext().theGame(); }
    
    private void optionValueChanged() {
        ui.theSound().play("audio.option.value_changed");
        idleTicks = 0;
    }

    private int selectedOption() {
        return selectedOption.get();
    }

    @Override
    public void handleKeyboardInput() {
        if (ui.theJoypad().isButtonPressed(JoypadButton.DOWN)) {
            selectedOption.set(selectedOption() + 1 < NUM_OPTIONS ? selectedOption() + 1 : 0);
        }
        else if (ui.theJoypad().isButtonPressed(JoypadButton.UP)) {
            selectedOption.set(selectedOption() == 0 ? NUM_OPTIONS - 1 : selectedOption() - 1);
        }
        // Button "A" on the joypad is located right of "B": select next value
        else if (ui.theJoypad().isButtonPressed(JoypadButton.A) || ui.theKeyboard().isPressed(KeyCode.RIGHT)) {
            switch (selectedOption()) {
                case OPTION_PAC_BOOSTER    -> setNextPacBoosterValue();
                case OPTION_DIFFICULTY     -> setNextDifficultyValue();
                case OPTION_MAZE_SELECTION -> setNextMapCategoryValue();
                case OPTION_STARTING_LEVEL -> setNextStartLevelValue();
            }
        }
        // Button "B" is left of "A": select previous value
        else if (ui.theJoypad().isButtonPressed(JoypadButton.B) || ui.theKeyboard().isPressed(KeyCode.LEFT)) {
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
        theGame().saveHighScore();
        optionValueChanged();
    }

    private void setNextMapCategoryValue() {
        MapCategory category = theGame().mapCategory();
        var values = MapCategory.values();
        int current = category.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        theGame().setMapCategory(values[next]);
        theGame().saveHighScore();
        optionValueChanged();
    }

    private void setPrevDifficultyValue() {
        Difficulty difficulty = theGame().difficulty();
        var values = Difficulty.values();
        int current = difficulty.ordinal(), prev = (current == 0) ? values.length - 1 : current - 1;
        theGame().setDifficulty(values[prev]);
        theGame().saveHighScore();
        optionValueChanged();
    }

    private void setNextDifficultyValue() {
        Difficulty difficulty = theGame().difficulty();
        var values = Difficulty.values();
        int current = difficulty.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        theGame().setDifficulty(values[next]);
        theGame().saveHighScore();
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

    @SuppressWarnings("unchecked")
    @Override
    public TengenMsPacMan_GameRenderer renderer() {
        return (TengenMsPacMan_GameRenderer) gameRenderer;
    }

    @Override
    public void drawSceneContent() {
        if (initialDelay > 0) {
            return;
        }
        renderer().ctx().setFont(scaledArcadeFont8());
        renderer().drawVerticalSceneBorders();

        var config = ui.<TengenMsPacMan_UIConfig>theConfiguration();
        if (config.propertyJoypadBindingsDisplayed.get()) {
            renderer().drawJoypadKeyBinding(ui.theJoypad().currentKeyBinding());
        }

        renderer().drawBar(nesPaletteColor(0x20), nesPaletteColor(0x21), sizeInPx().x(), 20);
        renderer().drawBar(nesPaletteColor(0x20), nesPaletteColor(0x21), sizeInPx().x(), 212);
        renderer().fillTextAtScaledPosition("MS PAC-MAN OPTIONS", NES_YELLOW, COL_LABEL + 3 * TS, 48);

        // Players (not implemented)
        drawMarkerIfSelected(OPTION_PLAYERS, 72, scaledArcadeFont8());
        renderer().fillTextAtScaledPosition("TYPE", NES_YELLOW, COL_LABEL, 72);
        renderer().fillTextAtScaledPosition(":", NES_YELLOW, COL_LABEL + 4 * TS + 4, 72);
        // grey out
        renderer().fillTextAtScaledPosition("1 PLAYER", nesPaletteColor(0x10), COL_LABEL + 6 * TS  , 72);

        // Pac-Booster
        drawMarkerIfSelected(OPTION_PAC_BOOSTER, 96, scaledArcadeFont8());
        renderer().fillTextAtScaledPosition("PAC BOOSTER", NES_YELLOW, COL_LABEL, 96);
        renderer().fillTextAtScaledPosition(":", NES_YELLOW, COL_COLON, 96);
        String pacBoosterText = switch (theGame().pacBooster()) {
            case OFF -> "OFF";
            case ALWAYS_ON -> "ALWAYS ON";
            case USE_A_OR_B -> "USE A OR B";
        };
        renderer().fillTextAtScaledPosition(pacBoosterText, NES_WHITE, COL_VALUE, 96);

        // Game difficulty
        drawMarkerIfSelected(OPTION_DIFFICULTY, 120, scaledArcadeFont8());
        renderer().fillTextAtScaledPosition("GAME DIFFICULTY", NES_YELLOW, COL_LABEL, 120);
        renderer().fillTextAtScaledPosition(":", NES_YELLOW, COL_COLON, 120);
        renderer().fillTextAtScaledPosition(theGame().difficulty().name(), NES_WHITE, COL_VALUE, 120);

        // Maze (type) selection
        drawMarkerIfSelected(OPTION_MAZE_SELECTION, 144, scaledArcadeFont8());
        renderer().fillTextAtScaledPosition("MAZE SELECTION", NES_YELLOW, COL_LABEL, 144);
        renderer().fillTextAtScaledPosition(":", NES_YELLOW, COL_COLON, 144);
        renderer().fillTextAtScaledPosition(theGame().mapCategory().name(), NES_WHITE, COL_VALUE, 144);

        // Starting level number
        drawMarkerIfSelected(OPTION_STARTING_LEVEL, 168, scaledArcadeFont8());
        renderer().fillTextAtScaledPosition("STARTING LEVEL", NES_YELLOW, COL_LABEL, 168);
        renderer().fillTextAtScaledPosition(":", NES_YELLOW, COL_COLON, 168);
        renderer().fillTextAtScaledPosition(String.valueOf(theGame().startLevelNumber()), NES_WHITE, COL_VALUE, 168);
        if (theGame().numContinues() < 4) {
            var spriteSheet = (TengenMsPacMan_SpriteSheet) ui.theConfiguration().spriteSheet();
            RectShort continuesSprite = spriteSheet.sprite(switch (theGame().numContinues()) {
                case 0 -> SpriteID.CONTINUES_0;
                case 1 -> SpriteID.CONTINUES_1;
                case 2 -> SpriteID.CONTINUES_2;
                case 3 -> SpriteID.CONTINUES_3;
                default -> throw new IllegalArgumentException("Illegal number of continues: " + theGame().numContinues());
            });
            renderer().drawSpriteScaled(continuesSprite, COL_VALUE + 3 * TS, 160);
        }

        renderer().fillTextAtScaledPosition("MOVE ARROW WITH JOYPAD", NES_YELLOW, 4 * TS,  192);
        renderer().fillTextAtScaledPosition("CHOOSE OPTIONS WITH A AND B", NES_YELLOW, 2 * TS,  200);
        renderer().fillTextAtScaledPosition("PRESS START TO START GAME", NES_YELLOW, 3 * TS,  208);
    }

    private void drawMarkerIfSelected(int optionIndex, double y, Font font) {
        if (selectedOption() == optionIndex) {
            ctx().setFill(NES_YELLOW);
            ctx().fillRect(scaled(COL_ARROW + 2.25), scaled(y - 4.5), scaled(7.5), scaled(1.75));
            renderer().fillTextAtScaledPosition(">", NES_YELLOW, font, COL_ARROW + 3, y);
        }
    }
}