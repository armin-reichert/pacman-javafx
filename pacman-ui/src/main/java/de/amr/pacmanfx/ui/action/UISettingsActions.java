/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.engine.action.ActionKeyBinding;
import de.amr.pacmanfx.engine.action.GameAction;
import de.amr.pacmanfx.engine.gamescene.GameSceneManager;
import de.amr.pacmanfx.ui.EngineContext;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.gamescene.common.DefaultGameSceneManager;
import javafx.beans.property.BooleanProperty;
import javafx.scene.input.KeyCode;

import java.util.Set;

import static de.amr.basics.util.Ufx.toggleBooleanProperty;
import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.bareKey;
import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.combine;

public class UISettingsActions {

    private final GameAction<EngineContext> actionEnterFullScreen;
    private final GameAction<EngineContext> actionShowHelp;
    private final GameAction<EngineContext> actionToggleDashboard;
    private final GameAction<EngineContext> actionToggleDebugInfo;
    private final GameAction<EngineContext> actionToggleKeyboardMonitor;
    private final GameAction<EngineContext> actionToggleMiniViewVisibility;
    private final GameAction<EngineContext> actionTogglePlayScene2D3D;

    private final Set<ActionKeyBinding<EngineContext>> bindings;

    public UISettingsActions() {

        actionEnterFullScreen = new GameAction<>("enter_fullscreen") {
            @Override
            public void execute(EngineContext context) {
                context.ui().setFullScreen(true);
            }
        };

        actionShowHelp = new GameAction<>("show_help") {
            @Override
            public void execute(EngineContext engine) {
                final String variantName = engine.gameVariantManager().currentVariantID();
                engine.viewManager().gamePlayView().showHelp(variantName);
            }

            @Override
            public boolean isEnabled(EngineContext engine) {
                final String variantName = engine.gameVariantManager().currentVariantID();
                final boolean isArcadeGame = GameVariantID.isArcadeGameName(variantName);
                final GameSceneManager gameSceneManager = engine.gameSceneManager();
                return isArcadeGame &&
                      (gameSceneManager.currentGameSceneHasID(CommonGameSceneID.INTRO_SCENE)
                    || gameSceneManager.currentGameSceneHasID(CommonGameSceneID.START_SCENE)
                    || gameSceneManager.currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_2D));
            }
        };

        actionToggleDashboard = new GameAction<>("toggle_dashboard") {
            @Override
            public void execute(EngineContext engine) {
                engine.viewManager().gamePlayView().dashboard().toggleVisibility();
            }

            @Override
            public boolean isEnabled(EngineContext engine) {
                return engine.viewManager().isGamePlayViewSelected();
            }
        };

        actionToggleDebugInfo = new GameAction<>("toggle_debug_info") {
            @Override
            public void execute(EngineContext engine) {
                toggleBooleanProperty(engine.ui().viewModel().debugModeOnProperty());
            }
        };

        actionToggleKeyboardMonitor = new GameAction<>("toggle_keyboard_monitor") {
            @Override
            public void execute(EngineContext engine) {
                toggleBooleanProperty(engine.ui().viewModel().keyboardMonitorOnProperty());
            }
        };

        actionToggleMiniViewVisibility = new GameAction<>("toggle_mini_view_visibility") {
            @Override
            public void execute(EngineContext engine) {
                final BooleanProperty miniViewActiveProperty = engine.ui().viewModel().miniViewSettings().activeProperty;
                toggleBooleanProperty(miniViewActiveProperty);
                // Message?
                if (!engine.gameSceneManager().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D)) {
                    final String msg = engine.translationManager().translate(
                        miniViewActiveProperty.get() ? "flash.pip_on" : "flash.pip_off");
                    engine.ui().showMessage(msg);
                }
            }
        };

        actionTogglePlayScene2D3D = new GameAction<>("toggle_play_scene_2d_3d") {
            @Override
            public void execute(EngineContext engine) {
                engine.optCurrentGame().ifPresent(game -> {
                    final BooleanProperty view3DEnabledProperty = engine.ui().viewModel().common3DSettings().view3DEnabledProperty();
                    toggleBooleanProperty(view3DEnabledProperty);
                    final boolean enabled = view3DEnabledProperty.get();
                    if (!isPlaySceneRunning(engine.gameSceneManager())) {
                        engine.ui().showMessage(engine.translationManager().translate(enabled ? "flash.use_3D_scene" : "flash.use_2D_scene"));
                    }
                    if (isLevelPlaying(game.state())) {
                        if (engine.gameSceneManager() instanceof DefaultGameSceneManager defaultGameSceneManager) {
                            //TODO This is dubious, must be revisited
                            defaultGameSceneManager.reloadGameScene(engine);
                        }
                    }
                });
            }

            @Override
            public boolean isEnabled(EngineContext engine) {
                return engine.viewManager().isGamePlayViewSelected();
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
            new ActionKeyBinding<>(actionEnterFullScreen(), bareKey(KeyCode.F11)),
            new ActionKeyBinding<>(actionShowHelp(), bareKey(KeyCode.H)),
            new ActionKeyBinding<>(actionToggleDashboard(), bareKey(KeyCode.F1), combine().alt().key(KeyCode.B)),
            new ActionKeyBinding<>(actionToggleDebugInfo(), combine().alt().key(KeyCode.D)),
            new ActionKeyBinding<>(actionToggleKeyboardMonitor(), combine().alt().key(KeyCode.K)),
            new ActionKeyBinding<>(actionToggleMiniViewVisibility(), bareKey(KeyCode.F2)),
            new ActionKeyBinding<>(actionTogglePlayScene2D3D(), combine().alt().key(KeyCode.DIGIT3), combine().alt().key(KeyCode.NUMPAD3))
        );
    }

    public GameAction<EngineContext> actionEnterFullScreen() {
        return actionEnterFullScreen;
    }

    public GameAction<EngineContext> actionShowHelp() {
        return actionShowHelp;
    }

    public GameAction<EngineContext> actionToggleDashboard() {
        return actionToggleDashboard;
    }

    public GameAction<EngineContext> actionToggleDebugInfo() {
        return actionToggleDebugInfo;
    }

    public GameAction<EngineContext> actionToggleKeyboardMonitor() {
        return actionToggleKeyboardMonitor;
    }

    public GameAction<EngineContext> actionToggleMiniViewVisibility() {
        return actionToggleMiniViewVisibility;
    }

    public GameAction<EngineContext> actionTogglePlayScene2D3D() {
        return actionTogglePlayScene2D3D;
    }

    public Set<ActionKeyBinding<EngineContext>> bindings() {
        return bindings;
    }
}
