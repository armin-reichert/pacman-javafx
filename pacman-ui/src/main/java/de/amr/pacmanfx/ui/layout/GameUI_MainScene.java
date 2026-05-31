/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Constants;
import de.amr.pacmanfx.ui.action.ActionBindingsSet;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.action.GameActionBindingsSet;
import de.amr.pacmanfx.ui.input.Input;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import org.tinylog.Logger;

public class GameUI_MainScene extends Scene {

    private final ActionBindingsSet actionBindings = new GameActionBindingsSet("Action Bindings for Main Scene");

    public GameUI_MainScene(double width, double height) {
        super(new StackPane(), width, height);
        rootPane().setPrefSize(width, height);
    }

    public void init(GameUI ui) {
        getStylesheets().add(GameUI_Constants.STYLE_SHEET_PATH);

        final Input userInput = Input.instance();

        addEventFilter(KeyEvent.KEY_PRESSED,  userInput.keyboard::onKeyPressed);
        addEventFilter(KeyEvent.KEY_RELEASED, userInput.keyboard::onKeyReleased);

        // If a global action can be executed, do it; otherwise let the current view handle it.
        userInput.keyboard.addStateListener(_ -> actionBindings.actionMatchingKeyboardState(userInput.keyboard).ifPresentOrElse(
            action -> action.executeIfEnabled(ui),
            () -> ui.services().subViews().currentView().onInput(ui, userInput)));

        // Delegate mouse scroll events to scene
        setOnScroll(e -> ui.services().gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.onScroll(e)));

        // Global action bindings
        actionBindings.registerFirstBinding(CommonActions.ACTION_ENTER_FULLSCREEN,        GameUI_Constants.COMMON_BINDINGS);
        actionBindings.registerFirstBinding(CommonActions.ACTION_OPEN_EDITOR,             GameUI_Constants.COMMON_BINDINGS);
        actionBindings.registerFirstBinding(CommonActions.ACTION_TOGGLE_KEYBOARD_MONITOR, GameUI_Constants.COMMON_BINDINGS);
        actionBindings.registerFirstBinding(CommonActions.ACTION_TOGGLE_MUTED,            GameUI_Constants.COMMON_BINDINGS);
        Logger.info(actionBindings);
    }

    public StackPane rootPane() {
        return (StackPane) getRoot();
    }
}