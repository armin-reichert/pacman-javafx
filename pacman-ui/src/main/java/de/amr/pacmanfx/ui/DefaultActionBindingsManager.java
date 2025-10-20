/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui.api.ActionBindingsManager;
import de.amr.pacmanfx.ui.input.Keyboard;
import javafx.scene.input.KeyCombination;
import org.tinylog.Logger;

import java.util.*;

import static java.util.Objects.requireNonNull;

public class DefaultActionBindingsManager implements ActionBindingsManager {

    public static class EmptyBindingsManager implements ActionBindingsManager {

        @Override
        public Map<KeyCombination, AbstractGameAction> actionByKeyCombination() {
            return Map.of();
        }

        @Override
        public boolean hasNoEntries() { return true; }

        @Override
        public void installBindings(Keyboard keyboard) {}

        @Override
        public void uninstallBindings(Keyboard keyboard) {}

        @Override
        public void useBindings(AbstractGameAction action, Set<ActionBinding> actionBindings) {}

        @Override
        public void setKeyCombination(AbstractGameAction action, KeyCombination combination) {}

        @Override
        public Optional<AbstractGameAction> matchingAction(Keyboard keyboard) { return Optional.empty(); }
    }

    public static final ActionBindingsManager EMPTY_BINDINGS_MANAGER = new EmptyBindingsManager();

    private final Map<KeyCombination, AbstractGameAction> actionByCombination;

    public DefaultActionBindingsManager() {
        this.actionByCombination = new HashMap<>();
    }

    @Override
    public Map<KeyCombination, AbstractGameAction> actionByKeyCombination() {
        return actionByCombination;
    }

    @Override
    public boolean hasNoEntries() {
        return actionByCombination.isEmpty();
    }

    @Override
    public void installBindings(Keyboard keyboard) {
        for (KeyCombination combination : actionByCombination.keySet()) {
            keyboard.setBinding(combination, this);
        }
        actionByCombination.entrySet().stream()
            // sort by string representation of key combination
            .sorted(Comparator.comparing(entry -> entry.getKey().toString()))
            .forEach(entry -> Logger.debug("%-20s: %s".formatted(entry.getKey(), entry.getValue().name())));
        Logger.info("Key bindings updated");
    }

    @Override
    public void uninstallBindings(Keyboard keyboard) {
        for (KeyCombination combination : actionByCombination.keySet()) {
            keyboard.removeBinding(combination, this);
        }
        Logger.info("Key bindings removed");
    }

    @Override
    public void setKeyCombination(AbstractGameAction action, KeyCombination combination) {
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
    @Override
    public void useBindings(AbstractGameAction gameAction, Set<ActionBinding> actionBindings) {
        requireNonNull(gameAction);
        requireNonNull(actionBindings);
        actionBindings.stream()
            .filter(actionBinding -> actionBinding.gameAction() == gameAction)
            .findFirst()
            .ifPresent(this::useBinding);
    }

    private void useBinding(ActionBinding actionBinding) {
        for (KeyCombination combination : actionBinding.keyCombinations()) {
            actionByCombination.put(combination, actionBinding.gameAction());
        }
    }

    @Override
    public Optional<AbstractGameAction> matchingAction(Keyboard keyboard) {
        return actionByCombination.keySet().stream()
            .filter(keyboard::isMatching)
            .map(actionByCombination::get)
            .findFirst();
    }
}