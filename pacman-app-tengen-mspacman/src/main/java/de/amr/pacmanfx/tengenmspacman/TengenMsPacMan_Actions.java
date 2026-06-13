/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.tengenmspacman.model.PacBooster;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.scenes.SceneDisplay;
import de.amr.pacmanfx.ui.action.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.CommonSceneID;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.input.JoypadButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import java.util.Objects;
import java.util.Set;

import static de.amr.pacmanfx.ui.input.Keyboard.*;
import static de.amr.pacmanfx.uilib.Ufx.toggleBooleanProperty;

public final class TengenMsPacMan_Actions {

    abstract class AbstractGameAction extends GameAction {

        protected AbstractGameAction(String key) {
            super(TengenMsPacMan_Actions.this.game, key);
        }
    }

    private final Game game;
    
    public TengenMsPacMan_Actions(Game game) {
        this.game = Objects.requireNonNull(game);

        STEERING_BINDINGS = Set.of(
            new ActionKeyBinding(game.actions().actionSteerUp,    keyFor(JoypadButton.UP),    control(KeyCode.UP)),
            new ActionKeyBinding(game.actions().actionSteerDown,  keyFor(JoypadButton.DOWN),  control(KeyCode.DOWN)),
            new ActionKeyBinding(game.actions().actionSteerLeft,  keyFor(JoypadButton.LEFT),  control(KeyCode.LEFT)),
            new ActionKeyBinding(game.actions().actionSteerRight, keyFor(JoypadButton.RIGHT), control(KeyCode.RIGHT))
        );

        TENGEN_LOCAL_BINDINGS = Set.of(
            new ActionKeyBinding(ACTION_QUIT_DEMO_LEVEL, keyFor(JoypadButton.START)),
            new ActionKeyBinding(ACTION_ENTER_START_SCREEN, keyFor(JoypadButton.START)),
            new ActionKeyBinding(ACTION_START_PLAYING, keyFor(JoypadButton.START)),
            new ActionKeyBinding(ACTION_TOGGLE_PAC_BOOSTER, keyFor(JoypadButton.A), keyFor(JoypadButton.B)),
            new ActionKeyBinding(ACTION_TOGGLE_PLAY_SCENE_DISPLAY_MODE, alt(KeyCode.C)),
            new ActionKeyBinding(ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY, bare(KeyCode.SPACE))
        );
    }

    public final GameAction ACTION_ENTER_START_SCREEN = new AbstractGameAction("enter_start_screen") {
        @Override
        public void doAction() {
            game.currentGameContext().flow().enterState(GameStateID.GAME_PREPARATION);
        }
    };

    public final GameAction ACTION_QUIT_DEMO_LEVEL = new AbstractGameAction("quit_demo_level") {
        @Override
        public void doAction() {
            game.currentGameContext().flow().enterState(GameStateID.GAME_PREPARATION);
        }

        @Override
        public boolean isEnabled() {
            final GameModel gameModel = game.currentGameContext().model();
            return gameModel.isDemoLevelRunning();
        }
    };

    public final GameAction ACTION_START_PLAYING = new AbstractGameAction("start_playing") {
        @Override
        public void doAction() {
            game.currentGameContext().flow().enterState(GameStateID.GAME_OR_LEVEL_STARTING);
        }
    };

    public final GameAction ACTION_TOGGLE_PLAY_SCENE_DISPLAY_MODE = new AbstractGameAction("toggle_play_scene_display_mode") {
        @Override
        public void doAction() {
            final var uiSettings = game.ui().extensions()
                .getExtension(TengenMsPacMan_UIConfig.EXT_UI_SETTINGS, TengenMsPacMan_UISettings.class);

            final SceneDisplay mode = uiSettings.playSceneDisplay.get();
            uiSettings.playSceneDisplay.set(mode == SceneDisplay.SCROLLING
                ? SceneDisplay.SCALED_TO_FIT
                : SceneDisplay.SCROLLING);
        }

        @Override
        public boolean isEnabled() {
            return game.ui().gameScenes().currentGameSceneHasID(game, CommonSceneID.PLAY_SCENE_2D);
        }
    };

    public final GameAction ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY = new AbstractGameAction("toggle_joypad_bindings_displayed") {
        @Override
        public void doAction() {
            final var uiSettings = game.ui().extensions()
                .getExtension(TengenMsPacMan_UIConfig.EXT_UI_SETTINGS, TengenMsPacMan_UISettings.class);

            toggleBooleanProperty(uiSettings.joypadBindingsDisplayed);
        }
    };

    public final GameAction ACTION_TOGGLE_PAC_BOOSTER = new AbstractGameAction("toggle_pac_booster") {
        @Override
        public void doAction() {
            game.currentGameContext().optCurrentLevel().ifPresent(gameLevel -> {
                final TengenMsPacMan_GameModel tengenGame = (TengenMsPacMan_GameModel) game.currentGameContext().model();
                tengenGame.activatePacBooster(gameLevel.entities().pac(), !tengenGame.isBoosterActive());
                if (tengenGame.isBoosterActive()) {
                    game.shortMessage("Booster!"); //TODO localize
                }
            });
        }

        @Override
        public boolean isEnabled() {
            final TengenMsPacMan_GameModel tengenGame = (TengenMsPacMan_GameModel) game.currentGameContext().model();
            return tengenGame.pacBoosterMode() == PacBooster.USE_A_OR_B && tengenGame.optGameLevel().isPresent();
        }
    };

    public final GameAction ACTION_SELECT_NEXT_JOYPAD_KEYBINDING = new AbstractGameAction("select_next_joypad_binding") {
        @Override
        public void doAction() {
            game.input().joypad().selectNextBinding();
        }
    };

    // Bindings

    private KeyCodeCombination keyFor(JoypadButton button) {
        return Input.instance().joypad().keyForButton(button);
    }

    public final Set<ActionKeyBinding> STEERING_BINDINGS;

    public final Set<ActionKeyBinding> TENGEN_LOCAL_BINDINGS;

}