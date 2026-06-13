/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.view;

import de.amr.pacmanfx.ui.GameUI_Constants;
import de.amr.pacmanfx.ui.action.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.action.GameActionBindingsMap;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.subviews.SubView;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class GameViewMainScene extends Scene {

    /** Index in the main scene's root pane child list where the active view is embedded. */
    public static final int SUBVIEW_INDEX = 0;

    private final ActionBindingsRegistry actionBindings = new GameActionBindingsMap("Action Bindings for Main Scene");

    public GameViewMainScene(double width, double height) {
        super(new StackPane(), width, height);
        rootPane().setPrefSize(width, height);
    }

    public void init(Game game) {
        getStylesheets().add(GameUI_Constants.STYLE_SHEET_PATH);
        
        final Keyboard keyboard = game.input().keyboard();
        keyboard.filterEventsForScene(this);
        keyboard.addStateListener(kb -> handleKeyboardStateChange(game, kb));

        // Delegate mouse scroll events to scene
        setOnScroll(e -> game.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.onScroll(e)));

        // Global action bindings
        actionBindings.selectAnyMatchingBinding(game.actions().actionEnterFullScreen(),       game.actions().commonBindings());
        actionBindings.selectAnyMatchingBinding(game.actions().actionOpenEditor(),            game.actions().commonBindings());
        actionBindings.selectAnyMatchingBinding(game.actions().actionToggleKeyboardMonitor(), game.actions().commonBindings());
        actionBindings.selectAnyMatchingBinding(game.actions().actionToggleMuted(),           game.actions().commonBindings());

        Logger.info(actionBindings);
    }

    private void handleKeyboardStateChange(Game game, Keyboard keyboard) {
        // Check for "global" action first. otherwise let current sub views handle the keyboard state change
        actionBindings.triggeredAction(keyboard).ifPresentOrElse(
            GameAction::execute,
            () -> game.ui().subViews().currentView().onInput(game, game.input()));
    }

    public StackPane rootPane() {
        return (StackPane) getRoot();
    }

    public void replaceSubView(SubView subView) {
        requireNonNull(subView);
        if (rootPane().getChildren().isEmpty()) {
            throw new IllegalStateException("Root pane has no placeholder for embedding view");
        }
        rootPane().getChildren().set(SUBVIEW_INDEX, subView.rootPane());
    }
}