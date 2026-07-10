/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.tengenmspacman.config.TengenMsPacMan_UISettings;
import de.amr.pacmanfx.tengenmspacman.gamescene.SceneDisplay;
import de.amr.pacmanfx.tengenmspacman.model.PacBooster;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.ui.action.SteeringActions;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.JoypadButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import java.util.Set;

import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.bareKey;
import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.combine;
import static de.amr.pacmanfx.uilib.Ufx.toggleBooleanProperty;

public final class TengenMsPacMan_Actions {

    private final GameAction actionEnterStartScreen;
    private final GameAction actionQuitDemoLevel;
    private final GameAction actionStartPlaying;
    private final GameAction actionTogglePlaySceneDisplayMode;
    private final GameAction actionToggleJoypadBindingsDisplayed;
    private final GameAction actionTogglePacBooster;
    private final GameAction actionSelectNextJoypadKeyBinding;

    private final Set<ActionKeyBinding> steeringBindings;
    private final Set<ActionKeyBinding> localBindings;

    public TengenMsPacMan_Actions(Game game) {
        final Joypad joypad = game.machine().input().joypad();

        actionEnterStartScreen = new GameAction(game, "enter_start_screen") {
            @Override
            public void doAction() {
                game.context().flow().enterState(GameStateID.GAME_PREPARATION);
            }
        };

        actionQuitDemoLevel = new GameAction(game, "quit_demo_level") {
            @Override
            public void doAction() {
                game.context().flow().enterState(GameStateID.GAME_PREPARATION);
            }

            @Override
            public boolean isEnabled() {
                return game.context().gamePlay().isDemoLevelRunning(game.context().model());
            }
        };

        actionStartPlaying = new GameAction(game, "start_playing") {
            @Override
            public void doAction() {
                game.context().flow().enterState(GameStateID.GAME_OR_LEVEL_STARTING);
            }
        };

        actionTogglePlaySceneDisplayMode = new GameAction(game, "toggle_play_scene_display_mode") {
            @Override
            public void doAction() {
                final var uiSettings = game.variantManager().selectedVariant()
                    .getExtensionValue(game, TengenMsPacMan_GameExtension.UI_SETTINGS, TengenMsPacMan_UISettings.class);

                final SceneDisplay mode = uiSettings.playSceneDisplay.get();
                uiSettings.playSceneDisplay.set(mode == SceneDisplay.SCROLLING
                    ? SceneDisplay.SCALED_TO_FIT
                    : SceneDisplay.SCROLLING);
            }

            @Override
            public boolean isEnabled() {
                return game.ui().gameScenes().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_2D);
            }
        };

        actionToggleJoypadBindingsDisplayed = new GameAction(game, "toggle_joypad_bindings_displayed") {
            @Override
            public void doAction() {
                final var uiSettings = game.variantManager().selectedVariant()
                    .getExtensionValue(game, TengenMsPacMan_GameExtension.UI_SETTINGS, TengenMsPacMan_UISettings.class);

                toggleBooleanProperty(uiSettings.joypadBindingsDisplayed);
            }
        };

        actionTogglePacBooster = new GameAction(game, "toggle_pac_booster") {
            @Override
            public void doAction() {
                game.context().model().optLevel().ifPresent(gameLevel -> {
                    final TengenMsPacMan_GameModel tengenGame = (TengenMsPacMan_GameModel) game.context().model();
                    tengenGame.activatePacBooster(gameLevel.entities().pac(), !tengenGame.isBoosterActive());
                    if (tengenGame.isBoosterActive()) {
                        game.ui().shortMessage("Booster!"); //TODO localize
                    }
                });
            }

            @Override
            public boolean isEnabled() {
                final TengenMsPacMan_GameModel tengenGame = (TengenMsPacMan_GameModel) game.context().model();
                return tengenGame.pacBoosterMode() == PacBooster.USE_A_OR_B && tengenGame.optLevel().isPresent();
            }
        };

        actionSelectNextJoypadKeyBinding = new GameAction(game, "select_next_joypad_binding") {
            @Override
            public void doAction() {
                game.machine().input().joypad().selectNextBinding();
            }
        };

        final SteeringActions steeringActions = game.actions().steeringActions();
        steeringBindings = Set.of(
            new ActionKeyBinding(steeringActions.actionSteer(Direction.UP),    keyForJoypadButton(joypad, JoypadButton.UP),    combine().ctrl().key(KeyCode.UP)),
            new ActionKeyBinding(steeringActions.actionSteer(Direction.DOWN),  keyForJoypadButton(joypad, JoypadButton.DOWN),  combine().ctrl().key(KeyCode.DOWN)),
            new ActionKeyBinding(steeringActions.actionSteer(Direction.LEFT),  keyForJoypadButton(joypad, JoypadButton.LEFT),  combine().ctrl().key(KeyCode.LEFT)),
            new ActionKeyBinding(steeringActions.actionSteer(Direction.RIGHT), keyForJoypadButton(joypad, JoypadButton.RIGHT), combine().ctrl().key(KeyCode.RIGHT))
        );

        localBindings = Set.of(
            new ActionKeyBinding(actionQuitDemoLevel(),                 keyForJoypadButton(joypad, JoypadButton.START)),
            new ActionKeyBinding(actionEnterStartScreen(),              keyForJoypadButton(joypad, JoypadButton.START)),
            new ActionKeyBinding(actionStartPlaying(),                  keyForJoypadButton(joypad, JoypadButton.START)),
            new ActionKeyBinding(actionTogglePacBooster(),              keyForJoypadButton(joypad, JoypadButton.A),
                                                                        keyForJoypadButton(joypad, JoypadButton.B)),
            new ActionKeyBinding(actionTogglePlaySceneDisplayMode(),    combine().alt().key(KeyCode.C)),
            new ActionKeyBinding(actionToggleJoypadBindingsDisplayed(), bareKey(KeyCode.SPACE))
        );
    }

    public Set<ActionKeyBinding> steeringBindings() {
        return steeringBindings;
    }

    public Set<ActionKeyBinding> localBindings() {
        return localBindings;
    }

    public GameAction actionEnterStartScreen() {
        return actionEnterStartScreen;
    }

    public GameAction actionQuitDemoLevel() {
        return actionQuitDemoLevel;
    }

    public GameAction actionStartPlaying() {
        return actionStartPlaying;
    }

    public GameAction actionTogglePlaySceneDisplayMode() {
        return actionTogglePlaySceneDisplayMode;
    }

    public GameAction actionToggleJoypadBindingsDisplayed() {
        return actionToggleJoypadBindingsDisplayed;
    }

    public GameAction actionTogglePacBooster() {
        return actionTogglePacBooster;
    }

    public GameAction actionSelectNextJoypadKeyBinding() {
        return actionSelectNextJoypadKeyBinding;
    }

    private static KeyCodeCombination keyForJoypadButton(Joypad joypad, JoypadButton button) {
        return joypad.keyForButton(button);
    }
}