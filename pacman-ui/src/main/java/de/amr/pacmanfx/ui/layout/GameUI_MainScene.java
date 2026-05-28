/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUIConstants;
import de.amr.pacmanfx.ui.action.ActionBindingsSet;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.input.Keyboard;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

public class GameUI_MainScene extends Scene {

    public GameUI_MainScene(double width, double height) {
        super(new StackPane(), width, height);
        rootPane().setPrefSize(width, height);
    }

    public void init(GameUI ui, ActionBindingsSet actionBindings) {
        getStylesheets().add(GameUIConstants.STYLE_SHEET_PATH);

        final Keyboard keyboard = Input.instance().keyboard;
        addEventFilter(KeyEvent.KEY_PRESSED,  keyboard::onKeyPressed);
        addEventFilter(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);

        // If a global action is bound to the key press, execute it; otherwise let the current view handle it.
        setOnKeyPressed(e -> actionBindings.matchingAction(keyboard)
            .ifPresentOrElse(action -> {
                if (action.executeIfEnabled(ui)) e.consume();
            },
            () -> ui.viewManager().currentView().onInput(ui))
        );

        // Delegate mouse scroll events to scene
        setOnScroll(e -> ui.gameSceneManager().optCurrentGameScene().ifPresent(gameScene -> gameScene.onScroll(e)));
    }

    public StackPane rootPane() {
        return (StackPane) getRoot();
    }
}