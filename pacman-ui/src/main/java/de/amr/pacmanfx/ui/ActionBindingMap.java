/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui.input.Keyboard;
import javafx.scene.input.KeyCombination;
import org.tinylog.Logger;

import java.util.*;

import static java.util.Objects.requireNonNull;

public class ActionBindingMap {

    public static final ActionBindingMap EMPTY_MAP = new ActionBindingMap() {
        @Override
        public boolean isEmpty() { return true; }

        @Override
        public Map<KeyCombination, GameAction> actionByCombination() { return Map.of(); }

        @Override
        public void updateKeyboard() {}

        @Override
        public void removeFromKeyboard() {}

        @Override
        public void use(GameAction action, List<ActionBinding> actionBindings) {}

        @Override
        public void bind(GameAction action, KeyCombination combination) {}

        @Override
        public Optional<GameAction> matchingAction() { return Optional.empty(); }
    };

    private Keyboard keyboard;
    private Map<KeyCombination, GameAction> actionByCombination;

    private ActionBindingMap() {}

    public ActionBindingMap(Keyboard keyboard) {
        this.keyboard = requireNonNull(keyboard);
        actionByCombination = new HashMap<>();
    }

    public Map<KeyCombination, GameAction> actionByCombination() {
        return actionByCombination;
    }

    public boolean isEmpty() {
        return actionByCombination.isEmpty();
    }

    public void updateKeyboard() {
        for (KeyCombination combination : actionByCombination.keySet()) {
            keyboard.setBinding(combination, this);
        }
        actionByCombination.entrySet().stream()
            // sort by string representation of key combination
            .sorted(Comparator.comparing(entry -> entry.getKey().toString()))
            .forEach(entry -> Logger.debug("%-20s: %s".formatted(entry.getKey(), entry.getValue().name())));
        Logger.info("Key bindings updated");
    }

    public void removeFromKeyboard() {
        for (KeyCombination combination : actionByCombination.keySet()) {
            keyboard.removeBinding(combination, this);
        }
        Logger.info("Key bindings removed");
    }

    public void addBinding(ActionBinding actionBinding) {
        for (KeyCombination combination : actionBinding.keyCombinations()) {
            bind(actionBinding.gameAction(), combination);
        }
    }

    public void bind(GameAction action, KeyCombination combination) {
        requireNonNull(action);
        requireNonNull(combination);
        actionByCombination.put(combination, action);
    }

    /**
     * Finds the first binding in the given list matching the given action and adds it to this map.
     * @param gameAction a game action
     * @param actionBindings an action bindings list
     */
    public void use(GameAction gameAction, List<ActionBinding> actionBindings) {
        requireNonNull(gameAction);
        requireNonNull(actionBindings);
        actionBindings.stream()
                .filter(actionBinding -> actionBinding.gameAction() == gameAction)
                .findFirst()
                .ifPresent(actionBinding -> {
                    for (KeyCombination combination : actionBinding.keyCombinations()) {
                        actionByCombination.put(combination, gameAction);
                    }
        });
    }

    public Optional<GameAction> matchingAction() {
        return actionByCombination.keySet().stream()
            .filter(keyboard::isMatching)
            .map(actionByCombination::get)
            .findFirst();
    }

    public void runMatchingAction(GameUI ui) {
        matchingAction().ifPresent(action -> action.executeIfEnabled(ui));
    }

    public void runMatchingActionOrElse(GameUI ui, Runnable defaultAction) {
        matchingAction().ifPresentOrElse(action -> action.executeIfEnabled(ui), defaultAction);
    }
}