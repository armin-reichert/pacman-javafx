/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.ui.input.Keyboard;
import javafx.scene.input.KeyCombination;
import org.tinylog.Logger;

import java.util.*;

import static java.util.Objects.requireNonNull;

public class ActionBindingsManagerImpl implements ActionBindingsManager {

    private final Map<KeyCombination, GameAction> keyCombinationToActionMap = new HashMap<>();

    public ActionBindingsManagerImpl() {}

    @Override
    public void dispose() {
        keyCombinationToActionMap.clear();
    }

    @Override
    public Map<KeyCombination, GameAction> keyCombinationToActionMap() {
        return keyCombinationToActionMap;
    }

    @Override
    public boolean empty() {
        return keyCombinationToActionMap.isEmpty();
    }

    @Override
    public void addAll(Keyboard keyboard) {
        for (KeyCombination combination : keyCombinationToActionMap.keySet()) {
            keyboard.setBinding(combination, this);
        }
        keyCombinationToActionMap.entrySet().stream()
            // sort by string representation of key combination
            .sorted(Comparator.comparing(entry -> entry.getKey().toString()))
            .forEach(entry -> Logger.debug("%-20s: %s".formatted(entry.getKey(), entry.getValue().name())));
        Logger.info("Key bindings updated");
    }

    @Override
    public void removeAll(Keyboard keyboard) {
        for (KeyCombination combination : keyCombinationToActionMap.keySet()) {
            keyboard.removeBinding(combination, this);
        }
        Logger.info("Key bindings removed");
    }

    @Override
    public void bindActionToKeyCombination(GameAction action, KeyCombination combination) {
        requireNonNull(action);
        requireNonNull(combination);
        keyCombinationToActionMap.put(combination, action);
    }

    @Override
    public void registerOne(GameAction gameAction, Set<ActionBinding> actionBindings) {
        requireNonNull(gameAction);
        requireNonNull(actionBindings);
        actionBindings.stream()
            .filter(actionBinding -> actionBinding.gameAction() == gameAction)
            .findAny()
            .ifPresent(this::registerBinding);
    }

    @Override
    public void registerAll(Set<ActionBinding> actionBindings) {
        for (ActionBinding binding : actionBindings) {
            registerBinding(binding);
        }
    }

    @Override
    public Optional<GameAction> findMatchingAction(Keyboard keyboard) {
        return keyCombinationToActionMap.keySet().stream()
            .filter(keyboard::isMatching)
            .map(keyCombinationToActionMap::get)
            .findFirst();
    }

    private void registerBinding(ActionBinding binding) {
        for (KeyCombination combination : binding.keyCombinations()) {
            keyCombinationToActionMap.put(combination, binding.gameAction());
        }
    }
}