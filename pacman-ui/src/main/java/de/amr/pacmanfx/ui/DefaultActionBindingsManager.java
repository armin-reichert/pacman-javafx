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

public class DefaultActionBindingsManager implements ActionBindingsManager {

    public static class EmptyManager implements ActionBindingsManager {

        @Override
        public Map<KeyCombination, GameAction> actionByKeyCombination() {
            return Map.of();
        }

        @Override
        public boolean hasNoEntries() { return true; }

        @Override
        public void updateKeyboard(Keyboard keyboard) {}

        @Override
        public void removeFromKeyboard(Keyboard keyboard) {}

        @Override
        public void addBinding(ActionBinding actionBinding) {}

        @Override
        public void useFirst(GameAction action, List<ActionBinding> actionBindings) {}

        @Override
        public void bind(GameAction action, KeyCombination combination) {}

        @Override
        public Optional<GameAction> matchingAction(Keyboard keyboard) { return Optional.empty(); }
    }

    public static final ActionBindingsManager EMPTY_MAP = new EmptyManager();

    protected Map<KeyCombination, GameAction> actionByCombination;

    public DefaultActionBindingsManager() {
        this.actionByCombination = new HashMap<>();
    }

    public Map<KeyCombination, GameAction> actionByKeyCombination() {
        return actionByCombination;
    }

    public boolean hasNoEntries() {
        return actionByCombination.isEmpty();
    }

    public void updateKeyboard(Keyboard keyboard) {
        for (KeyCombination combination : actionByCombination.keySet()) {
            keyboard.setBinding(combination, this);
        }
        actionByCombination.entrySet().stream()
            // sort by string representation of key combination
            .sorted(Comparator.comparing(entry -> entry.getKey().toString()))
            .forEach(entry -> Logger.debug("%-20s: %s".formatted(entry.getKey(), entry.getValue().name())));
        Logger.info("Key bindings updated");
    }

    public void removeFromKeyboard(Keyboard keyboard) {
        for (KeyCombination combination : actionByCombination.keySet()) {
            keyboard.removeBinding(combination, this);
        }
        Logger.info("Key bindings removed");
    }

    public void addBinding(ActionBinding actionBinding) {
        for (KeyCombination combination : actionBinding.keyCombinations()) {
            actionByCombination.put(combination, actionBinding.gameAction());
        }
    }

    public void bind(GameAction action, KeyCombination combination) {
        requireNonNull(action);
        requireNonNull(combination);
        actionByCombination.put(combination, action);
    }

    /**
     * Finds the first binding in the given list matching the given action and adds it to this map.
     *
     * @param gameAction a game action
     * @param actionBindings an action bindings list
     */
    public void useFirst(GameAction gameAction, List<ActionBinding> actionBindings) {
        requireNonNull(gameAction);
        requireNonNull(actionBindings);
        actionBindings.stream()
            .filter(actionBinding -> actionBinding.gameAction() == gameAction)
            .findFirst()
            .ifPresent(this::addBinding);
    }

    public Optional<GameAction> matchingAction(Keyboard keyboard) {
        return actionByCombination.keySet().stream()
            .filter(keyboard::isMatching)
            .map(actionByCombination::get)
            .findFirst();
    }
}