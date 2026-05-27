/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.ui.input.Keyboard;
import javafx.scene.input.KeyCodeCombination;
import org.tinylog.Logger;

import java.util.*;

import static java.util.Objects.requireNonNull;

public class GameActionBindingsManager implements ActionBindingsManager {

    private static final Map<KeyCodeCombination, ActionBindingsManager> actionBindingsManagerRegistry = new HashMap<>();

    public static void registerActionBindingsManager(KeyCodeCombination combination, ActionBindingsManager bindingsManager) {
        requireNonNull(combination);
        requireNonNull(bindingsManager);
        if (actionBindingsManagerRegistry.get(combination) == bindingsManager) {
            Logger.debug("Key combination '{}' already bound to {}", combination, bindingsManager);
        } else {
            actionBindingsManagerRegistry.put(combination, bindingsManager);
            Logger.debug("Key combination '{}' bound to {}", combination, bindingsManager);
        }
    }

    public static void unregisterActionBindingsManager(KeyCodeCombination combination, ActionBindingsManager bindingsManager) {
        requireNonNull(combination);
        requireNonNull(bindingsManager);
        boolean removed = actionBindingsManagerRegistry.remove(combination, bindingsManager);
        if (removed) {
            Logger.debug("Key code combination '{}' unbound from {}", combination, bindingsManager);
        }
    }

    private final Keyboard keyboard;
    private final Map<KeyCodeCombination, GameAction> actionForKeyCombination = new HashMap<>();

    public GameActionBindingsManager(Keyboard keyboard) {
        this.keyboard = requireNonNull(keyboard);
    }

    @Override
    public void dispose() {
        actionForKeyCombination.clear();
    }

    @Override
    public Map<KeyCodeCombination, GameAction> actionForKeyCombination() {
        return actionForKeyCombination;
    }

    @Override
    public boolean noBindings() {
        return actionForKeyCombination.isEmpty();
    }

    @Override
    public void register() {
        for (KeyCodeCombination combination : actionForKeyCombination.keySet()) {
            registerActionBindingsManager(combination, this);
        }
        logBindings();
    }

    @Override
    public void unregister() {
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
    public void registerAllBindings(Set<ActionBinding> bindings) {
        requireNonNull(bindings);
        for (ActionBinding binding : bindings) {
            registerBinding(binding);
        }
    }

    @Override
    public Optional<GameAction> matchingAction() {
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