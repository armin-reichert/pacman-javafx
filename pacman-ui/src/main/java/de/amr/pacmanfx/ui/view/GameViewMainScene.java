/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.view;

import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.AppConstants;
import de.amr.pacmanfx.ui.action.ActionBindingsSet;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.action.GameActionBindingsSet;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.subviews.SubView;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class GameViewMainScene extends Scene {

    /** Index in the main scene's root pane child list where the active view is embedded. */
    public static final int SUBVIEW_INDEX = 0;

    private final ActionBindingsSet actionBindings = new GameActionBindingsSet("Action Bindings for Main Scene");

    public GameViewMainScene(double width, double height) {
        super(new StackPane(), width, height);
        rootPane().setPrefSize(width, height);
    }

    public void init(AppContext context) {
        getStylesheets().add(AppConstants.STYLE_SHEET_PATH);

        final Input userInput = Input.instance();

        addEventFilter(KeyEvent.KEY_PRESSED,  userInput.keyboard::onKeyPressed);
        addEventFilter(KeyEvent.KEY_RELEASED, userInput.keyboard::onKeyReleased);

        // If a global action can be executed, do it; otherwise let the current view handle it.
        userInput.keyboard.addStateListener(_ -> actionBindings.actionMatchingKeyboardState(userInput.keyboard).ifPresentOrElse(
            action -> action.executeIfEnabled(context),
            () -> context.ui().subViews().currentView().onInput(context, userInput)));

        // Delegate mouse scroll events to scene
        setOnScroll(e -> context.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.onScroll(e)));

        // Global action bindings
        actionBindings.registerFirstBinding(CommonActions.ACTION_ENTER_FULLSCREEN,        AppConstants.COMMON_BINDINGS);
        actionBindings.registerFirstBinding(CommonActions.ACTION_OPEN_EDITOR,             AppConstants.COMMON_BINDINGS);
        actionBindings.registerFirstBinding(CommonActions.ACTION_TOGGLE_KEYBOARD_MONITOR, AppConstants.COMMON_BINDINGS);
        actionBindings.registerFirstBinding(CommonActions.ACTION_TOGGLE_MUTED,            AppConstants.COMMON_BINDINGS);

        Logger.info("\n" + actionBindings);
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