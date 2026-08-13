/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneManager;
import de.amr.pacmanfx.ui.views.GameViewID;
import javafx.scene.input.KeyCode;

import java.util.Set;

import static de.amr.basics.util.Ufx.toggleBooleanProperty;
import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.bareKey;
import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.combine;

public class UISettingsActions {

    private final GameAction actionEnterFullScreen;
    private final GameAction actionShowHelp;
    private final GameAction actionToggleDashboard;
    private final GameAction actionToggleDebugInfo;
    private final GameAction actionToggleKeyboardMonitor;
    private final GameAction actionToggleMiniViewVisibility;
    private final GameAction actionTogglePlayScene2D3D;

    private final Set<ActionKeyBinding> bindings;

    public UISettingsActions() {

        actionEnterFullScreen = new GameAction("enter_fullscreen") {
            @Override
            public void doAction(GameAppContext app) {
                app.ui().setFullScreenMode(true);
            }
        };

        actionShowHelp = new GameAction("show_help") {
            @Override
            public void doAction(GameAppContext app) {
                app.ui().views().gamePlayView().showHelp(app);
            }

            @Override
            public boolean isEnabled(GameAppContext app) {
                final GameSceneManager gameScenes = app.ui().gameScenes();
                final String variantName = app.gameVariants().currentVariantName();
                final boolean isArcadeGame = GameVariantID.isArcadeGameName(variantName);
                return isArcadeGame &&
                    (gameScenes.currentGameSceneHasID(CommonGameSceneID.INTRO_SCENE)
                        || gameScenes.currentGameSceneHasID(CommonGameSceneID.START_SCENE)
                        || gameScenes.currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_2D));
            }
        };

        actionToggleDashboard = new GameAction("toggle_dashboard") {
            @Override
            public void doAction(GameAppContext app) {
                app.ui().views().gamePlayView().dashboard().toggleVisibility();
            }

            @Override
            public boolean isEnabled(GameAppContext app) {
                return app.ui().views().isSelected(GameViewID.GAMEPLAY);
            }
        };

        actionToggleDebugInfo = new GameAction("toggle_debug_info") {
            @Override
            public void doAction(GameAppContext app) {
                toggleBooleanProperty(app.ui().viewModel().debugModeOnProperty);
            }
        };

        actionToggleKeyboardMonitor = new GameAction("toggle_keyboard_monitor") {
            @Override
            public void doAction(GameAppContext app) {
                toggleBooleanProperty(app.ui().viewModel().keyboardMonitorOnProperty);
            }
        };

        actionToggleMiniViewVisibility = new GameAction("toggle_mini_view_visibility") {
            @Override
            public void doAction(GameAppContext app) {
                toggleBooleanProperty(app.ui().viewModel().miniView.activeProperty);
                if (!app.ui().gameScenes().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D)) {
                    final String msg = app.ui().translations().translate(
                        app.ui().viewModel().miniView.activeProperty.get() ? "flash.pip_on" : "flash.pip_off");
                    app.ui().shortMessage(msg);
                }
            }
        };

        actionTogglePlayScene2D3D = new GameAction("toggle_play_scene_2d_3d") {
            @Override
            public void doAction(GameAppContext app) {
                final GameContext game = app.game();
                toggleBooleanProperty(app.ui().viewModel().common3D.view3DEnabledProperty);
                final boolean is3DEnabled = app.ui().viewModel().common3D.view3DEnabledProperty.get();
                if (!inPlayScene(app)) {
                    app.ui().shortMessage(app.ui().translations().translate(is3DEnabled
                        ? "flash.use_3D_scene" : "flash.use_2D_scene"));
                }
                if (isLevelPlaying(game.session())) {
                    app.ui().gameScenes().forceGameSceneUpdate();
                }
            }

            @Override
            public boolean isEnabled(GameAppContext app) {
                return app.ui().views().isSelected(GameViewID.GAMEPLAY);
            }

            private boolean inPlayScene(GameAppContext app) {
                final GameSceneManager gameScenes = app.ui().gameScenes();
                return gameScenes.currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_2D)
                    || gameScenes.currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D);
            }

            private boolean isLevelPlaying(GameSession session) {
                final GameState gameState = session.gameState();
                return CommonGameStateID.GAME_LEVEL_PLAYING.hasSameNameAs(gameState);
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
