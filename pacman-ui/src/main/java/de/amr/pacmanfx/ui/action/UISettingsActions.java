/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneManager;
import de.amr.pacmanfx.ui.views.GameViewID;
import javafx.scene.input.KeyCode;

import java.util.Set;

import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.bareKey;
import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.combine;
import static de.amr.pacmanfx.uilib.Ufx.toggleBooleanProperty;

public class UISettingsActions {

    private final GameAction actionEnterFullScreen;
    private final GameAction actionShowHelp;
    private final GameAction actionToggleDashboard;
    private final GameAction actionToggleDebugInfo;
    private final GameAction actionToggleKeyboardMonitor;
    private final GameAction actionToggleMiniViewVisibility;
    private final GameAction actionTogglePlayScene2D3D;

    private final Set<ActionKeyBinding> bindings;

    public UISettingsActions(Game game) {

        actionEnterFullScreen = new GameAction(game, "enter_fullscreen") {
            @Override
            protected void doAction() {
                game.ui().window().stage().setFullScreen(true);
            }
        };

        actionShowHelp = new GameAction(game, "show_help") {
            @Override
            protected void doAction() {
                game.ui().views().gamePlayView().showHelp(game);
            }

            @Override
            public boolean isEnabled() {
                final GameSceneManager gameScenes = game.ui().gameScenes();
                final String variantName = game.variants().selectedVariantName();
                final boolean isArcadeGame = GameVariantID.isArcadeGameName(variantName);
                return isArcadeGame &&
                    (gameScenes.currentGameSceneHasID(CommonGameSceneID.INTRO_SCENE)
                        || gameScenes.currentGameSceneHasID(CommonGameSceneID.START_SCENE)
                        || gameScenes.currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_2D));
            }
        };

        actionToggleDashboard = new GameAction(game, "toggle_dashboard") {
            @Override
            protected void doAction() {
                game.ui().views().gamePlayView().dashboard().toggleVisibility();
            }

            @Override
            public boolean isEnabled() {
                return game.ui().views().isSelected(GameViewID.GAMEPLAY);
            }
        };

        actionToggleDebugInfo = new GameAction(game, "toggle_debug_info") {
            @Override
            protected void doAction() {
                toggleBooleanProperty(game.ui().viewModel().debugModeOnProperty);
            }
        };

        actionToggleKeyboardMonitor = new GameAction(game, "toggle_keyboard_monitor") {
            @Override
            protected void doAction() {
                toggleBooleanProperty(game.ui().viewModel().keyboardMonitorOnProperty);
            }
        };

        actionToggleMiniViewVisibility = new GameAction(game, "toggle_mini_view_visibility") {
            @Override
            protected void doAction() {
                toggleBooleanProperty(game.ui().viewModel().miniView.activeProperty);
                if (!game.ui().gameScenes().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D)) {
                    final String msg = game.ui().translations().translate(
                        game.ui().viewModel().miniView.activeProperty.get() ? "flash.pip_on" : "flash.pip_off");
                    game.ui().shortMessage(msg);
                }
            }
        };

        actionTogglePlayScene2D3D = new GameAction(game, "toggle_play_scene_2d_3d") {
            @Override
            protected void doAction() {
                toggleBooleanProperty(game.ui().viewModel().common3D.view3DEnabledProperty);
                final boolean is3DEnabled = game.ui().viewModel().common3D.view3DEnabledProperty.get();
                if (!inPlayScene()) {
                    game.ui().shortMessage(game.ui().translations().translate(is3DEnabled
                        ? "flash.use_3D_scene" : "flash.use_2D_scene"));
                }
                if (isLevelPlaying()) {
                    game.ui().gameScenes().forceGameSceneUpdate();
                }
            }

            @Override
            public boolean isEnabled() {
                return game.ui().views().isSelected(GameViewID.GAMEPLAY);
            }

            private boolean inPlayScene() {
                final GameSceneManager gameScenes = game.ui().gameScenes();
                return gameScenes.currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_2D)
                    || gameScenes.currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D);
            }

            private boolean isLevelPlaying() {
                final GameState gameState = game.context().state();
                return GameStateID.GAME_LEVEL_PLAYING.identifies(gameState);
            }
        };

        bindings = Set.of(
            new ActionKeyBinding(actionEnterFullScreen(), bareKey(KeyCode.F11)),
            new ActionKeyBinding(actionShowHelp(), bareKey(KeyCode.H)),
            new ActionKeyBinding(actionToggleDashboard, bareKey(KeyCode.F1), combine().alt().key(KeyCode.B)),
            new ActionKeyBinding(actionToggleDebugInfo, combine().alt().key(KeyCode.D)),
            new ActionKeyBinding(actionToggleKeyboardMonitor, combine().alt().key(KeyCode.K)),
            new ActionKeyBinding(actionToggleMiniViewVisibility, bareKey(KeyCode.F2)),
            new ActionKeyBinding(actionTogglePlayScene2D3D(), combine().alt().key(KeyCode.DIGIT3), combine().alt().key(KeyCode.NUMPAD3))
        );
    }

    public GameAction actionEnterFullScreen() {
        return actionEnterFullScreen;
    }

    public GameAction actionShowHelp() {
        return actionShowHelp;
    }

    public GameAction actionToggleDashboard() {
        return actionToggleDashboard;
    }

    public GameAction actionToggleDebugInfo() {
        return actionToggleDebugInfo;
    }

    public GameAction actionToggleKeyboardMonitor() {
        return actionToggleKeyboardMonitor;
    }

    public GameAction actionToggleMiniViewVisibility() {
        return actionToggleMiniViewVisibility;
    }

    public GameAction actionTogglePlayScene2D3D() {
        return actionTogglePlayScene2D3D;
    }

    public Set<ActionKeyBinding> bindings() {
        return bindings;
    }
}
