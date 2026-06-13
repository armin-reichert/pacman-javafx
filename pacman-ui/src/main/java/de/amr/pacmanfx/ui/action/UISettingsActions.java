/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.common.CommonSceneID;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneManager;
import de.amr.pacmanfx.ui.subviews.SubViewManager;
import javafx.scene.input.KeyCode;

import java.util.Set;

import static de.amr.pacmanfx.ui.input.Keyboard.alt;
import static de.amr.pacmanfx.ui.input.Keyboard.bare;
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
                game.ui().view().stage().setFullScreen(true);
            }
        };

        actionShowHelp = new GameAction(game, "show_help") {
            @Override
            protected void doAction() {
                game.ui().subViews().gamePlayView().showHelp(game);
            }

            @Override
            public boolean isEnabled() {
                final GameSceneManager gameScenes = game.ui().gameScenes();
                final String variantName = game.currentGameVariantName();
                final boolean isArcadeGame = GameVariantID.isArcadeGameName(variantName);
                return isArcadeGame &&
                    (gameScenes.currentGameSceneHasID(game, CommonSceneID.INTRO_SCENE)
                        || gameScenes.currentGameSceneHasID(game, CommonSceneID.START_SCENE)
                        || gameScenes.currentGameSceneHasID(game, CommonSceneID.PLAY_SCENE_2D));
            }
        };

        actionToggleDashboard = new GameAction(game, "toggle_dashboard") {
            @Override
            protected void doAction() {
                game.ui().subViews().gamePlayView().dashboard().toggleVisibility();
            }

            @Override
            public boolean isEnabled() {
                final SubViewManager subViews = game.ui().subViews();
                return subViews.isSelected(subViews.gamePlayView());
            }
        };

        actionToggleDebugInfo = new GameAction(game, "toggle_debug_info") {
            @Override
            protected void doAction() {
                toggleBooleanProperty(game.ui().settings().debugInfoVisibleProperty);
            }
        };

        actionToggleKeyboardMonitor = new GameAction(game, "toggle_keyboard_monitor") {
            @Override
            protected void doAction() {
                toggleBooleanProperty(game.ui().settings().keyboardMonitorVisibleProperty);
            }
        };

        actionToggleMiniViewVisibility = new GameAction(game, "toggle_mini_view_visibility") {
            @Override
            protected void doAction() {
                toggleBooleanProperty(game.ui().settings().miniViewOnProperty);
                if (!game.ui().gameScenes().currentGameSceneHasID(game, CommonSceneID.PLAY_SCENE_3D)) {
                    final String msg = game.ui().translations().translate(
                        game.ui().settings().miniViewOnProperty.get() ? "pip_on" : "pip_off");
                    game.shortMessage(msg);
                }
            }
        };

        actionTogglePlayScene2D3D = new GameAction(game, "toggle_play_scene_2d_3d") {
            @Override
            protected void doAction() {
                toggleBooleanProperty(game.ui().settings3D().view3DEnabledProperty());
                final boolean is3DEnabled = game.ui().settings3D().view3DEnabledProperty().get();
                if (!inPlayScene()) {
                    game.shortMessage(game.ui().translations().translate(is3DEnabled ? "use_3D_scene" : "use_2D_scene"));
                }
                if (isLevelPlaying()) {
                    game.ui().gameScenes().forceGameSceneUpdate(game);
                }
            }

            @Override
            public boolean isEnabled() {
                final SubViewManager subViews = game.ui().subViews();
                return subViews.isSelected(subViews.gamePlayView());
            }

            private boolean inPlayScene() {
                final GameSceneManager gameScenes = game.ui().gameScenes();
                return gameScenes.currentGameSceneHasID(game, CommonSceneID.PLAY_SCENE_2D)
                    || gameScenes.currentGameSceneHasID(game, CommonSceneID.PLAY_SCENE_3D);
            }

            private boolean isLevelPlaying() {
                final GameState gameState = game.currentGameContext().state();
                return GameStateID.GAME_LEVEL_PLAYING.identifies(gameState);
            }
        };

        bindings = Set.of(
            new ActionKeyBinding(actionEnterFullScreen(), bare(KeyCode.F11)),
            new ActionKeyBinding(actionShowHelp(), bare(KeyCode.H)),
            new ActionKeyBinding(actionToggleDashboard, bare(KeyCode.F1), alt(KeyCode.B)),
            new ActionKeyBinding(actionToggleDebugInfo, alt(KeyCode.D)),
            new ActionKeyBinding(actionToggleKeyboardMonitor, alt(KeyCode.K)),
            new ActionKeyBinding(actionToggleMiniViewVisibility, bare(KeyCode.F2)),
            new ActionKeyBinding(actionTogglePlayScene2D3D(), alt(KeyCode.DIGIT3), alt(KeyCode.NUMPAD3))
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
