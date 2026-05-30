/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUIConstants;
import de.amr.pacmanfx.ui.action.ActionBindingsSet;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.action.GameActionBindingsSet;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.input.Keyboard;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

import static de.amr.pacmanfx.ui.input.Keyboard.bare;

public class GameUI_MainScene extends Scene {

    private final ActionBindingsSet actionBindings = new GameActionBindingsSet();

    public GameUI_MainScene(double width, double height) {
        super(new StackPane(), width, height);
        rootPane().setPrefSize(width, height);
    }

    public void init(GameUI ui) {
        getStylesheets().add(GameUIConstants.STYLE_SHEET_PATH);

        actionBindings.registerAnyBindingFromSet(CommonActions.ACTION_ENTER_FULLSCREEN,        GameUIConstants.COMMON_BINDINGS);
        actionBindings.registerAnyBindingFromSet(CommonActions.ACTION_OPEN_EDITOR,             GameUIConstants.COMMON_BINDINGS);
        actionBindings.registerAnyBindingFromSet(CommonActions.ACTION_TOGGLE_KEYBOARD_MONITOR, GameUIConstants.COMMON_BINDINGS);
        actionBindings.registerAnyBindingFromSet(CommonActions.ACTION_TOGGLE_MUTED,            GameUIConstants.COMMON_BINDINGS);
        actionBindings.activate();


        final Input userInput = Input.instance();

        addEventFilter(KeyEvent.KEY_PRESSED,  userInput.keyboard::onKeyPressed);
        addEventFilter(KeyEvent.KEY_RELEASED, userInput.keyboard::onKeyReleased);

        // If a global action is bound to the key press, execute it; otherwise let the current view handle it.
        setOnKeyPressed(e -> actionBindings.matchingAction(userInput.keyboard)
            .ifPresentOrElse(action -> {
                if (action.executeIfEnabled(ui)) e.consume();
            },
            () -> ui.viewManager().currentView().onInput(ui, userInput))
        );

        // Delegate mouse scroll events to scene
        setOnScroll(e -> ui.gameSceneManager().optCurrentGameScene().ifPresent(gameScene -> gameScene.onScroll(e)));
    }

    public StackPane rootPane() {
        return (StackPane) getRoot();
    }
}