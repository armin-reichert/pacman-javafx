/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.tinylog.Logger;

import java.util.Map;
import java.util.Optional;

import static de.amr.games.pacman.ui.Globals.THE_UI;

public interface ActionProvider {

    Map<KeyCodeCombination, Action> actionBindings();

    void bindGameActions();

    default void enableActionBindings() {
        Logger.info("Enabled key bindings for {}", getClass().getSimpleName());
        for (KeyCodeCombination keyCodeCombination : actionBindings().keySet()) {
            THE_UI.keyboard().register(keyCodeCombination, this);
        }
    }

    default void disableActionBindings() {
        Logger.info("Disable key bindings for {}", getClass().getSimpleName());
        for (KeyCodeCombination keyCodeCombination : actionBindings().keySet()) {
            THE_UI.keyboard().unregister(keyCodeCombination, this);
        }
    }

    default void bind(Action action, KeyCodeCombination... combinations) {
        for (KeyCodeCombination kcc : combinations) {
            actionBindings().put(kcc, action);
        }
    }

    default void bind(Action action, KeyCode... keyCodes) {
        for (KeyCode keyCode : keyCodes) {
            actionBindings().put(new KeyCodeCombination(keyCode), action);
        }
    }

    default Optional<Action> firstTriggeredAction() {
        return actionBindings().keySet().stream()
            .filter(combination -> THE_UI.keyboard().isMatching(combination))
            .map(actionBindings()::get)
            .findFirst();
    }

    default void runTriggeredAction() {
        firstTriggeredAction().ifPresent(action -> {
            if (action.isEnabled()) {
                action.execute();
            } else {
                Logger.info("Disabled action '{}' not executed", action);
            }
        });
    }

    default void runTriggeredActionElse(Runnable defaultAction) {
        firstTriggeredAction().ifPresentOrElse(action -> {
            if (action.isEnabled()) {
                action.execute();
            } else {
                Logger.info("Disabled action '{}' not executed", action);
            }
        }, defaultAction);
    }

    default void handleInput() {
        runTriggeredAction();
    }
}