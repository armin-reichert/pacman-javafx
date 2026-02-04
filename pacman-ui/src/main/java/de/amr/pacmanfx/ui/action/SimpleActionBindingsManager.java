/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.ui.ActionBindingsManager;
import de.amr.pacmanfx.ui.input.Keyboard;
import javafx.scene.input.KeyCombination;
import org.tinylog.Logger;

import java.util.*;

import static java.util.Objects.requireNonNull;

public class SimpleActionBindingsManager implements ActionBindingsManager {

    private final Map<KeyCombination, GameAction> actionForKeyCombination = new HashMap<>();

    public SimpleActionBindingsManager() {}

    @Override
    public void dispose() {
        actionForKeyCombination.clear();
    }

    @Override
    public Map<KeyCombination, GameAction> actionRegisteredForKeyCombination() {
        return actionForKeyCombination;
    }

    @Override
    public boolean isEmpty() {
        return actionForKeyCombination.isEmpty();
    }

    @Override
    public void addAll(Keyboard keyboard) {
        for (KeyCombination combination : actionForKeyCombination.keySet()) {
            keyboard.setBinding(combination, this);
        }
        actionForKeyCombination.entrySet().stream()
            // sort by string representation of key combination
            .sorted(Comparator.comparing(entry -> entry.getKey().toString()))
            .forEach(entry -> Logger.debug("%-20s: %s".formatted(entry.getKey(), entry.getValue().name())));
        Logger.info("Key bindings updated");
    }

    @Override
    public void removeAll(Keyboard keyboard) {
        for (KeyCombination combination : actionForKeyCombination.keySet()) {
            keyboard.removeBinding(combination, this);
        }
        Logger.info("Key bindings removed");
    }

    @Override
    public void registerByKeyCombination(GameAction action, KeyCombination combination) {
        requireNonNull(action);
        requireNonNull(combination);
        actionForKeyCombination.put(combination, action);
    }

    @Override
    public void registerAnyFrom(GameAction gameAction, Set<ActionBinding> actionBindings) {
        requireNonNull(gameAction);
        requireNonNull(actionBindings);
        actionBindings.stream()
            .filter(actionBinding -> actionBinding.gameAction() == gameAction)
            .findAny()
            .ifPresent(this::registerBinding);
    }

    @Override
    public void registerAllFrom(Set<ActionBinding> actionBindings) {
        for (ActionBinding binding : actionBindings) {
            registerBinding(binding);
        }
    }

    @Override
    public Optional<GameAction> findMatchingAction(Keyboard keyboard) {
        return actionForKeyCombination.keySet().stream()
            .filter(keyboard::isMatching)
            .map(actionForKeyCombination::get)
            .findFirst();
    }

    private void registerBinding(ActionBinding binding) {
        for (KeyCombination combination : binding.keyCombinations()) {
            actionForKeyCombination.put(combination, binding.gameAction());
        }
    }
}