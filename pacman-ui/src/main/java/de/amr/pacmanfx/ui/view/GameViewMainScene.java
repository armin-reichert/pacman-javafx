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
import de.amr.pacmanfx.ui.subviews.SubViewManager;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class GameViewMainScene extends Scene {

    /** Index in the main scene's root pane child list where the active view is embedded. */
    public static final int SUBVIEW_CHILD_INDEX = 0;

    private final ActionBindingsRegistry actionBindings = new GameActionBindingsMap("Main Scene Action Bindings");

    public GameViewMainScene(double width, double height) {
        super(new StackPane(), width, height, Color.BLACK);
        getStylesheets().add(GameUI_Constants.STYLE_SHEET_PATH);
    }

    public void init(Game game) {
        final SubViewManager subViews = game.ui().subViews();
        final Keyboard keyboard = game.input().keyboard();

        // Keyboard should not be sensitive to any key events triggered inside the map editor
        keyboard.enabledProperty().bind(subViews.selectedSubViewProperty().map(
            _ -> subViews.isSelected(subViews.startView()) || subViews.isSelected(subViews.gamePlayView())
        ));

        keyboard.addStateListener(_ -> {
            // Check for "global" action first, if no one matches, let current sub view handle the keyboard state change
            actionBindings.triggeredAction(game.input().keyboard()).ifPresentOrElse(
                GameAction::execute,
                () -> game.ui().subViews().currentView().onInput(game, game.input()));
        });

        keyboard.filterKeyEventsFrom(this);

        // Delegate mouse scroll events to current game scene
        setOnScroll(e -> game.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.onScroll(e)));

        // Register common action bindings
        actionBindings.selectAnyMatchingBinding(game.actions().uiSettingsActions().actionToggleKeyboardMonitor(), game.actions().commonBindings());
        actionBindings.selectAnyMatchingBinding(game.actions().uiSettingsActions().actionEnterFullScreen(), game.actions().commonBindings());
        actionBindings.selectAnyMatchingBinding(game.actions().simulationActions().actionToggleMuted(), game.actions().commonBindings());
        actionBindings.selectAnyMatchingBinding(game.actions().editorActions().actionOpenEditor(), game.actions().commonBindings());
        Logger.info(actionBindings);
    }

    public StackPane rootPane() {
        return (StackPane) getRoot();
    }

    public void replaceSubView(SubView subView) {
        requireNonNull(subView);
        if (rootPane().getChildren().isEmpty()) {
            throw new IllegalStateException("Root pane has no placeholder for embedding view");
        }
        rootPane().getChildren().set(SUBVIEW_CHILD_INDEX, subView.rootPane());
    }
}