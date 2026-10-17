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
import de.amr.pacmanfx.ui.EngineUIContext;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.gamescene.common.DefaultGameSceneManager;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.playview.GamePlayView;
import javafx.beans.property.BooleanProperty;
import javafx.scene.input.KeyCode;

import java.util.Set;

import static de.amr.basics.util.Ufx.toggleBooleanProperty;
import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.bareKey;
import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.combine;

public class UISettingsActions {

    private final GameAction<EngineUIContext> actionEnterFullScreen;
    private final GameAction<EngineUIContext> actionShowHelp;
    private final GameAction<EngineUIContext> actionToggleDashboard;
    private final GameAction<EngineUIContext> actionToggleDebugInfo;
    private final GameAction<EngineUIContext> actionToggleKeyboardMonitor;
    private final GameAction<EngineUIContext> actionToggleMiniViewVisibility;
    private final GameAction<EngineUIContext> actionTogglePlayScene2D3D;

    private final Set<ActionKeyBinding<EngineUIContext>> bindings;

    public UISettingsActions() {

        actionEnterFullScreen = new GameAction<>("enter_fullscreen") {
            @Override
            public void execute(EngineUIContext context) {
                context.ui().window().setFullScreen(true);
            }
        };

        actionShowHelp = new GameAction<>("show_help") {
            @Override
            public void execute(EngineUIContext engineContext) {
                final String variantName = engineContext.gameVariantManager().currentVariantName();
                engineContext.viewManager().assertView(GameViewID.GAMEPLAY, GamePlayView.class).showHelp(variantName);
            }

            @Override
            public boolean isEnabled(EngineUIContext context) {
                final String variantName = context.gameVariantManager().currentVariantName();
                final boolean isArcadeGame = GameVariantID.isArcadeGameName(variantName);
                final GameSceneManager gameSceneManager = context.gameSceneManager();
                return isArcadeGame &&
                      (gameSceneManager.currentGameSceneHasID(CommonGameSceneID.INTRO_SCENE)
                    || gameSceneManager.currentGameSceneHasID(CommonGameSceneID.START_SCENE)
                    || gameSceneManager.currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_2D));
            }
        };

        actionToggleDashboard = new GameAction<>("toggle_dashboard") {
            @Override
            public void execute(EngineUIContext engineContext) {
                engineContext.viewManager().assertView(GameViewID.GAMEPLAY, GamePlayView.class).dashboard().toggleVisibility();
            }

            @Override
            public boolean isEnabled(EngineUIContext engineContext) {
                return engineContext.viewManager().isSelected(GameViewID.GAMEPLAY);
            }
        };

        actionToggleDebugInfo = new GameAction<>("toggle_debug_info") {
            @Override
            public void execute(EngineUIContext context) {
                toggleBooleanProperty(context.ui().viewModel().debugModeOnProperty());
            }
        };

        actionToggleKeyboardMonitor = new GameAction<>("toggle_keyboard_monitor") {
            @Override
            public void execute(EngineUIContext context) {
                toggleBooleanProperty(context.ui().viewModel().keyboardMonitorOnProperty());
            }
        };

        actionToggleMiniViewVisibility = new GameAction<>("toggle_mini_view_visibility") {
            @Override
            public void execute(EngineUIContext context) {
                final BooleanProperty miniViewActiveProperty = context.ui().viewModel().miniViewSettings().activeProperty;
                toggleBooleanProperty(miniViewActiveProperty);
                // Message?
                if (!context.gameSceneManager().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D)) {
                    final String msg = context.translationManager().translate(
                        miniViewActiveProperty.get() ? "flash.pip_on" : "flash.pip_off");
                    context.ui().showShortMessage(msg);
                }
            }
        };

        actionTogglePlayScene2D3D = new GameAction<>("toggle_play_scene_2d_3d") {
            @Override
            public void execute(EngineUIContext engineContext) {
                engineContext.optCurrentGame().ifPresent(game -> {
                    final BooleanProperty view3DEnabledProperty = engineContext.ui().viewModel().common3DSettings().view3DEnabledProperty();
                    toggleBooleanProperty(view3DEnabledProperty);
                    final boolean enabled = view3DEnabledProperty.get();
                    if (!isPlaySceneRunning(engineContext.gameSceneManager())) {
                        engineContext.ui().showShortMessage(engineContext.translationManager().translate(enabled ? "flash.use_3D_scene" : "flash.use_2D_scene"));
                    }
                    if (isLevelPlaying(game.state())) {
                        if (engineContext.gameSceneManager() instanceof DefaultGameSceneManager defaultGameSceneManager) {
                            //TODO This is dubious, must be revisited
                            defaultGameSceneManager.forceGameSceneUpdate(engineContext);
                        }
                    }
                });
            }

            @Override
            public boolean isEnabled(EngineUIContext engineContext) {
                return engineContext.viewManager().isSelected(GameViewID.GAMEPLAY);
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

    public GameAction<EngineUIContext> actionEnterFullScreen() {
        return actionEnterFullScreen;
    }

    public GameAction<EngineUIContext> actionShowHelp() {
        return actionShowHelp;
    }

    public GameAction<EngineUIContext> actionToggleDashboard() {
        return actionToggleDashboard;
    }

    public GameAction<EngineUIContext> actionToggleDebugInfo() {
        return actionToggleDebugInfo;
    }

    public GameAction<EngineUIContext> actionToggleKeyboardMonitor() {
        return actionToggleKeyboardMonitor;
    }

    public GameAction<EngineUIContext> actionToggleMiniViewVisibility() {
        return actionToggleMiniViewVisibility;
    }

    public GameAction<EngineUIContext> actionTogglePlayScene2D3D() {
        return actionTogglePlayScene2D3D;
    }

    public Set<ActionKeyBinding<EngineUIContext>> bindings() {
        return bindings;
    }
}
