/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.model.GamePlayState;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.Difficulty;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.tengen.ms_pacman.model.PacBooster;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_OptionsScene_Renderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.action.TestActions;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.uilib.rendering.HUD_Renderer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;

import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Actions.ACTION_START_PLAYING;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Actions.ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.ui.input.Keyboard.alt;

/**
 * Options scene for Ms. Pac-Man Tengen.
 *
 * <p></p>The highscore is cleared if player type (1 player, 2 players etc.), map category or difficulty are
 * changed, see <a href="https://github.com/RussianManSMWC/Ms.-Pac-Man-NES-Tengen-Disassembly/blob/main/MsPacManTENGENDis.asm:9545">disassembly</a>.
 */
public class TengenMsPacMan_OptionsScene extends GameScene2D {

    public static final byte OPTION_PLAYERS = 0;
    public static final byte OPTION_PAC_BOOSTER = 1;
    public static final byte OPTION_DIFFICULTY = 2;
    public static final byte OPTION_MAZE_SELECTION = 3;
    public static final byte OPTION_STARTING_LEVEL = 4;

    public static final byte NUM_OPTIONS = 5;

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
            ui.joypad().selectNextBinding(actionBindings);
        }
    };

    private TengenMsPacMan_OptionsScene_Renderer sceneRenderer;

    private int idleTicks;
    public int initialDelay;

    public TengenMsPacMan_OptionsScene(GameUI ui) {
        super(ui);
    }

    @Override
    protected void createRenderers(Canvas canvas) {
        sceneRenderer = GameScene2D_Renderer.configureRendererForGameScene(
            new TengenMsPacMan_OptionsScene_Renderer(this, canvas, (TengenMsPacMan_SpriteSheet) ui.currentConfig().spriteSheet()), this);
    }

    @Override
    protected HUD_Renderer hudRenderer() {
        return null;
    }

    @Override
    public TengenMsPacMan_OptionsScene_Renderer sceneRenderer() {
        return sceneRenderer;
    }

    @Override
    public void doInit() {
        context().currentGame().hud().all(false);

        var tengenActionBindings = ui.<TengenMsPacMan_UIConfig>currentConfig().tengenActionBindings();
        actionBindings.addKeyCombination(actionSelectNextJoypadBinding, alt(KeyCode.J)); //TODO
        actionBindings.bind(ACTION_START_PLAYING, tengenActionBindings);
        actionBindings.bind(ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY, tengenActionBindings);
        actionBindings.bind(TestActions.ACTION_CUT_SCENES_TEST, ui.actionBindings());
        actionBindings.bind(TestActions.ACTION_SHORT_LEVEL_TEST, ui.actionBindings());
        actionBindings.bind(TestActions.ACTION_MEDIUM_LEVEL_TEST, ui.actionBindings());
        ui.joypad().setBindings(actionBindings);

        selectedOption.set(OPTION_PAC_BOOSTER);
        theGame().setCanStartNewGame(true);

        idleTicks = 0;
        initialDelay = INITIAL_DELAY;
    }

    @Override
    protected void doEnd() {
        ui.joypad().removeBindings(actionBindings);
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
            context().currentGame().stateMachine().changeState(GamePlayState.INTRO);
        }
    }

    @Override
    public Vector2i sizeInPx() { return NES_SIZE_PX; }

    private TengenMsPacMan_GameModel theGame() { return context().currentGame(); }
    
    private void optionValueChanged() {
        ui.soundManager().play("audio.option.value_changed");
        idleTicks = 0;
    }

    public int selectedOption() {
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
}