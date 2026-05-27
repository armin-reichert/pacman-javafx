/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.ui.input.Keyboard;
import javafx.scene.input.KeyCodeCombination;
import org.tinylog.Logger;

import java.util.*;

import static java.util.Objects.requireNonNull;

public class GameActionBindingsSet implements ActionBindingsSet {

    private static final Map<KeyCodeCombination, ActionBindingsSet> actionBindingsSetRegistry = new HashMap<>();

    public static void registerActionBindingsManager(KeyCodeCombination combination, ActionBindingsSet bindings) {
        requireNonNull(combination);
        requireNonNull(bindings);
        if (actionBindingsSetRegistry.get(combination) == bindings) {
            Logger.debug("Key combination '{}' already bound to {}", combination, bindings);
        } else {
            actionBindingsSetRegistry.put(combination, bindings);
            Logger.debug("Key combination '{}' bound to {}", combination, bindings);
        }
    }

    public static void unregisterActionBindingsManager(KeyCodeCombination combination, ActionBindingsSet bindings) {
        requireNonNull(combination);
        requireNonNull(bindings);
        boolean removed = actionBindingsSetRegistry.remove(combination, bindings);
        if (removed) {
            Logger.debug("Key code combination '{}' unbound from {}", combination, bindings);
        }
    }

    private final Map<KeyCodeCombination, GameAction> actionForKeyCombination = new HashMap<>();

    public GameActionBindingsSet() {}

    @Override
    public void dispose() {
        actionForKeyCombination.clear();
    }

    @Override
    public Map<KeyCodeCombination, GameAction> actionForKeyCombination() {
        return actionForKeyCombination;
    }

    @Override
    public boolean isEmpty() {
        return actionForKeyCombination.isEmpty();
    }

    @Override
    public void activate() {
        for (KeyCodeCombination combination : actionForKeyCombination.keySet()) {
            registerActionBindingsManager(combination, this);
        }
        logBindings();
    }

    @Override
    public void deactivate() {
        for (KeyCodeCombination combination : actionForKeyCombination.keySet()) {
            unregisterActionBindingsManager(combination, this);
        }
    }

    @Override
    public void setKeyCombinationFor(GameAction action, KeyCodeCombination combination) {
        requireNonNull(action);
        requireNonNull(combination);
        actionForKeyCombination.put(combination, action);
    }

    @Override
    public void registerAnyBindingFromSet(GameAction gameAction, Set<ActionBinding> bindings) {
        requireNonNull(gameAction);
        requireNonNull(bindings);
        bindings.stream()
            .filter(actionBinding -> actionBinding.gameAction() == gameAction)
            .findAny()
            .ifPresent(this::registerBinding);
    }

    @Override
    public void registerAllBindingsFromSet(Set<ActionBinding> bindings) {
        requireNonNull(bindings);
        for (ActionBinding binding : bindings) {
            registerBinding(binding);
        }
    }

    @Override
    public Optional<GameAction> matchingAction(Keyboard keyboard) {
        return actionForKeyCombination.keySet().stream()
            .filter(keyboard::stateMatches)
            .map(actionForKeyCombination::get)
            .findFirst();
    }

    private void registerBinding(ActionBinding binding) {
        requireNonNull(binding);
        for (KeyCodeCombination combination : binding.keyCombinations()) {
            actionForKeyCombination.put(combination, binding.gameAction());
        }
    }

    private void logBindings() {
        // Sort output by key combination display text
        actionForKeyCombination.entrySet().stream()
            .sorted(Comparator.comparing(e -> e.getKey().toString()))
            .forEach(e -> Logger.info("%-20s: %s".formatted(e.getKey(), e.getValue().resourceBundleKey())));
    }
}