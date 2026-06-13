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
import de.amr.pacmanfx.ui.gamescene.common.CommonSceneID;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.input.JoypadButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import java.util.Objects;
import java.util.Set;

import static de.amr.pacmanfx.ui.input.Keyboard.*;
import static de.amr.pacmanfx.uilib.Ufx.toggleBooleanProperty;

public final class TengenMsPacMan_Actions {

    private final Game game;

    private final Set<ActionKeyBinding> steeringBindings;
    private final Set<ActionKeyBinding> localBindings;

    public TengenMsPacMan_Actions(Game game) {
        this.game = Objects.requireNonNull(game);

        steeringBindings = Set.of(
            new ActionKeyBinding(game.actions().steeringActions().actionSteerUp(),    keyFor(JoypadButton.UP),    control(KeyCode.UP)),
            new ActionKeyBinding(game.actions().steeringActions().actionSteerDown(),  keyFor(JoypadButton.DOWN),  control(KeyCode.DOWN)),
            new ActionKeyBinding(game.actions().steeringActions().actionSteerLeft(),  keyFor(JoypadButton.LEFT),  control(KeyCode.LEFT)),
            new ActionKeyBinding(game.actions().steeringActions().actionSteerRight(), keyFor(JoypadButton.RIGHT), control(KeyCode.RIGHT))
        );

        localBindings = Set.of(
            new ActionKeyBinding(actionQuitDemoLevel(), keyFor(JoypadButton.START)),
            new ActionKeyBinding(actionEnterStartScreen(), keyFor(JoypadButton.START)),
            new ActionKeyBinding(actionStartPlaying(), keyFor(JoypadButton.START)),
            new ActionKeyBinding(actionTogglePacBooster(), keyFor(JoypadButton.A), keyFor(JoypadButton.B)),
            new ActionKeyBinding(actionTogglePlaySceneDisplayMode(), alt(KeyCode.C)),
            new ActionKeyBinding(actionToggleJoypadBindingsDisplayed(), bare(KeyCode.SPACE))
        );
    }

    public Set<ActionKeyBinding> steeringBindings() {
        return steeringBindings;
    }

    public Set<ActionKeyBinding> localBindings() {
        return localBindings;
    }

    private GameAction actionEnterStartScreen;

    public GameAction actionEnterStartScreen() {
        if (actionEnterStartScreen == null) {
            actionEnterStartScreen = new GameAction(game, "enter_start_screen") {
                @Override
                public void doAction() {
                    game.currentGameContext().flow().enterState(GameStateID.GAME_PREPARATION);
                }
            };
        }
        return actionEnterStartScreen;
    }

    private GameAction actionQuitDemoLevel;

    public GameAction actionQuitDemoLevel() {
        if (actionQuitDemoLevel == null) {
            actionQuitDemoLevel = new GameAction(game, "quit_demo_level") {
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
        }
        return actionQuitDemoLevel;
    }

    private GameAction actionStartPlaying;

    public GameAction actionStartPlaying() {
        if  (actionStartPlaying == null) {
            actionStartPlaying = new GameAction(game, "start_playing") {
                @Override
                public void doAction() {
                    game.currentGameContext().flow().enterState(GameStateID.GAME_OR_LEVEL_STARTING);
                }
            };
        }
        return actionStartPlaying;
    }

    private GameAction actionTogglePlaySceneDisplayMode;

    public GameAction actionTogglePlaySceneDisplayMode() {
        if (actionTogglePlaySceneDisplayMode == null) {
            actionTogglePlaySceneDisplayMode = new GameAction(game, "toggle_play_scene_display_mode") {
                @Override
                public void doAction() {
                    final var uiSettings = game.extensions().get(TengenMsPacMan_GameExtension.UI_SETTINGS, TengenMsPacMan_UISettings.class);

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
        }
        return actionTogglePlaySceneDisplayMode;
    }

    private GameAction actionToggleJoypadBindingsDisplayed;

    public GameAction actionToggleJoypadBindingsDisplayed() {
        if (actionToggleJoypadBindingsDisplayed == null) {
            actionToggleJoypadBindingsDisplayed = new GameAction(game, "toggle_joypad_bindings_displayed") {
                @Override
                public void doAction() {
                    final var uiSettings = game.extensions().get(TengenMsPacMan_GameExtension.UI_SETTINGS, TengenMsPacMan_UISettings.class);

                    toggleBooleanProperty(uiSettings.joypadBindingsDisplayed);
                }
            };
        }
        return actionToggleJoypadBindingsDisplayed;
    }

    private GameAction actionTogglePacBooster;

    public GameAction actionTogglePacBooster() {
        if (actionTogglePacBooster == null) {
            actionTogglePacBooster = new GameAction(game, "toggle_pac_booster") {
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
        }
        return actionTogglePacBooster;
    }

    private GameAction actionSelectNextJoypadKeyBinding;

    public GameAction actionSelectNextJoypadKeyBinding() {
        if (actionSelectNextJoypadKeyBinding == null) {
            actionSelectNextJoypadKeyBinding = new GameAction(game, "select_next_joypad_binding") {
                @Override
                public void doAction() {
                    game.input().joypad().selectNextBinding();
                }
            };
        }
        return actionSelectNextJoypadKeyBinding;
    }

    // Bindings

    private KeyCodeCombination keyFor(JoypadButton button) {
        return Input.instance().joypad().keyForButton(button);
    }

}