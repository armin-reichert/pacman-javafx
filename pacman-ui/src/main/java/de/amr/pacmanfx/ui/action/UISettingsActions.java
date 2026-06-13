/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.CommonSceneID;
import de.amr.pacmanfx.ui.gamescene.GameSceneManager;
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

        bindings = Set.of(
            new ActionKeyBinding(actionEnterFullScreen(), bare(KeyCode.F11)),
            new ActionKeyBinding(actionShowHelp(), bare(KeyCode.H)),
            new ActionKeyBinding(actionToggleDashboard, bare(KeyCode.F1), alt(KeyCode.B)),
            new ActionKeyBinding(actionToggleDebugInfo, alt(KeyCode.D)),
            new ActionKeyBinding(actionToggleKeyboardMonitor, alt(KeyCode.K)),
            new ActionKeyBinding(actionToggleMiniViewVisibility, bare(KeyCode.F2))
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

    public Set<ActionKeyBinding> bindings() {
        return bindings;
    }
}
