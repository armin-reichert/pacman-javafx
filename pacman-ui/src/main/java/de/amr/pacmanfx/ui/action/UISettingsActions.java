/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneManager;
import de.amr.pacmanfx.ui.views.GameViewID;
import javafx.beans.property.BooleanProperty;
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
            public void execute(GameAppContext app) {
                app.ui().window().setFullScreen(true);
            }
        };

        actionShowHelp = new GameAction("show_help") {
            @Override
            public void execute(GameAppContext app) {
                app.ui().viewManager().gamePlayView().showHelp(app);
            }

            @Override
            public boolean isEnabled(GameAppContext app) {
                final String variantName = app.variantManager().currentVariantName();
                final boolean isArcadeGame = GameVariantID.isArcadeGameName(variantName);
                return isArcadeGame &&
                      (app.gameSceneManager().currentGameSceneHasID(CommonGameSceneID.INTRO_SCENE)
                    || app.gameSceneManager().currentGameSceneHasID(CommonGameSceneID.START_SCENE)
                    || app.gameSceneManager().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_2D));
            }
        };

        actionToggleDashboard = new GameAction("toggle_dashboard") {
            @Override
            public void execute(GameAppContext app) {
                app.ui().viewManager().gamePlayView().dashboard().toggleVisibility();
            }

            @Override
            public boolean isEnabled(GameAppContext app) {
                return app.ui().viewManager().isSelected(GameViewID.GAMEPLAY);
            }
        };

        actionToggleDebugInfo = new GameAction("toggle_debug_info") {
            @Override
            public void execute(GameAppContext app) {
                toggleBooleanProperty(app.ui().viewModel().debugModeOnProperty());
            }
        };

        actionToggleKeyboardMonitor = new GameAction("toggle_keyboard_monitor") {
            @Override
            public void execute(GameAppContext app) {
                toggleBooleanProperty(app.ui().viewModel().keyboardMonitorOnProperty());
            }
        };

        actionToggleMiniViewVisibility = new GameAction("toggle_mini_view_visibility") {
            @Override
            public void execute(GameAppContext app) {
                final BooleanProperty miniViewActiveProperty = app.ui().viewModel().miniViewSettings().activeProperty;
                toggleBooleanProperty(miniViewActiveProperty);
                // Message?
                if (!app.gameSceneManager().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D)) {
                    final String msg = app.ui().translationManager().translate(
                        miniViewActiveProperty.get() ? "flash.pip_on" : "flash.pip_off");
                    app.ui().shortMessage(msg);
                }
            }
        };

        actionTogglePlayScene2D3D = new GameAction("toggle_play_scene_2d_3d") {
            @Override
            public void execute(GameAppContext app) {
                final GameContext game = app.game();
                final BooleanProperty view3DEnabledProperty = app.ui().viewModel().common3DSettings().view3DEnabledProperty();
                toggleBooleanProperty(view3DEnabledProperty);
                final boolean enabled = view3DEnabledProperty.get();
                if (!isPlaySceneRunning(app.gameSceneManager())) {
                    app.ui().shortMessage(app.ui().translationManager().translate(enabled ? "flash.use_3D_scene" : "flash.use_2D_scene"));
                }
                if (isLevelPlaying(game.state())) {
                    app.gameSceneManager().forceGameSceneUpdate(app);
                }
            }

            @Override
            public boolean isEnabled(GameAppContext app) {
                return app.ui().viewManager().isSelected(GameViewID.GAMEPLAY);
            }

            private boolean isPlaySceneRunning(GameSceneManager gameSceneManager) {
                return gameSceneManager.currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_2D)
                    || gameSceneManager.currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D);
            }

            private boolean isLevelPlaying(AbstractGameState gameState) {
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
