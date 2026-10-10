/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.engine.action.GameAction;
import de.amr.pacmanfx.engine.runtime.action.ActionKeyBinding;
import de.amr.pacmanfx.engine.runtime.action.GameEngineContext;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneManager;
import de.amr.pacmanfx.ui.views.GameViewID;
import javafx.beans.property.BooleanProperty;
import javafx.scene.input.KeyCode;

import java.util.Set;

import static de.amr.basics.util.Ufx.toggleBooleanProperty;
import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.bareKey;
import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.combine;

public class UISettingsActions {

    private final GameAction<GameEngineContext> actionEnterFullScreen;
    private final GameAction<GameEngineContext> actionShowHelp;
    private final GameAction<GameEngineContext> actionToggleDashboard;
    private final GameAction<GameEngineContext> actionToggleDebugInfo;
    private final GameAction<GameEngineContext> actionToggleKeyboardMonitor;
    private final GameAction<GameEngineContext> actionToggleMiniViewVisibility;
    private final GameAction<GameEngineContext> actionTogglePlayScene2D3D;

    private final Set<ActionKeyBinding> bindings;

    public UISettingsActions() {

        actionEnterFullScreen = new GameAction<>("enter_fullscreen") {
            @Override
            public void execute(GameEngineContext context) {
                context.ui().window().setFullScreen(true);
            }
        };

        actionShowHelp = new GameAction<>("show_help") {
            @Override
            public void execute(GameEngineContext actionContext) {
                actionContext.ui().viewManager().gamePlayView().showHelp(actionContext);
            }

            @Override
            public boolean isEnabled(GameEngineContext context) {
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
            public void execute(GameEngineContext context) {
                context.ui().viewManager().gamePlayView().dashboard().toggleVisibility();
            }

            @Override
            public boolean isEnabled(GameEngineContext context) {
                return context.ui().viewManager().isSelected(GameViewID.GAMEPLAY);
            }
        };

        actionToggleDebugInfo = new GameAction<>("toggle_debug_info") {
            @Override
            public void execute(GameEngineContext context) {
                toggleBooleanProperty(context.ui().viewModel().debugModeOnProperty());
            }
        };

        actionToggleKeyboardMonitor = new GameAction<>("toggle_keyboard_monitor") {
            @Override
            public void execute(GameEngineContext context) {
                toggleBooleanProperty(context.ui().viewModel().keyboardMonitorOnProperty());
            }
        };

        actionToggleMiniViewVisibility = new GameAction<>("toggle_mini_view_visibility") {
            @Override
            public void execute(GameEngineContext context) {
                final BooleanProperty miniViewActiveProperty = context.ui().viewModel().miniViewSettings().activeProperty;
                toggleBooleanProperty(miniViewActiveProperty);
                // Message?
                if (!context.gameSceneManager().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D)) {
                    final String msg = context.translationManager().translate(
                        miniViewActiveProperty.get() ? "flash.pip_on" : "flash.pip_off");
                    context.ui().shortMessage(msg);
                }
            }
        };

        actionTogglePlayScene2D3D = new GameAction<>("toggle_play_scene_2d_3d") {
            @Override
            public void execute(GameEngineContext actionContext) {
                final GameContext game = actionContext.currentGame();
                final BooleanProperty view3DEnabledProperty = actionContext.ui().viewModel().common3DSettings().view3DEnabledProperty();
                toggleBooleanProperty(view3DEnabledProperty);
                final boolean enabled = view3DEnabledProperty.get();
                if (!isPlaySceneRunning(actionContext.gameSceneManager())) {
                    actionContext.ui().shortMessage(actionContext.translationManager().translate(enabled ? "flash.use_3D_scene" : "flash.use_2D_scene"));
                }
                if (isLevelPlaying(game.state())) {
                    //TODO This is dubious
                    actionContext.gameSceneManager().forceGameSceneUpdate(actionContext);
                }
            }

            @Override
            public boolean isEnabled(GameEngineContext context) {
                return context.ui().viewManager().isSelected(GameViewID.GAMEPLAY);
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

    public GameAction<GameEngineContext> actionEnterFullScreen() {
        return actionEnterFullScreen;
    }

    public GameAction<GameEngineContext> actionShowHelp() {
        return actionShowHelp;
    }

    public GameAction<GameEngineContext> actionToggleDashboard() {
        return actionToggleDashboard;
    }

    public GameAction<GameEngineContext> actionToggleDebugInfo() {
        return actionToggleDebugInfo;
    }

    public GameAction<GameEngineContext> actionToggleKeyboardMonitor() {
        return actionToggleKeyboardMonitor;
    }

    public GameAction<GameEngineContext> actionToggleMiniViewVisibility() {
        return actionToggleMiniViewVisibility;
    }

    public GameAction<GameEngineContext> actionTogglePlayScene2D3D() {
        return actionTogglePlayScene2D3D;
    }

    public Set<ActionKeyBinding> bindings() {
        return bindings;
    }
}
