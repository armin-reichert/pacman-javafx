/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.ui.input.Input;
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
    public void addToKeyboard() {
        for (KeyCombination combination : keyCombinationToActionMap.keySet()) {
            Input.instance().keyboard.setBinding(combination, this);
        }
        logBindings();
        Logger.info("Key bindings updated");
    }

    @Override
    public void removeFromKeyboard() {
        for (KeyCombination combination : keyCombinationToActionMap.keySet()) {
            Input.instance().keyboard.removeBinding(combination, this);
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

    private void logBindings() {
        // Sort output by key combination display text
        keyCombinationToActionMap.entrySet().stream()
            .sorted(Comparator.comparing(e -> e.getKey().toString()))
            .forEach(e -> Logger.debug("%-20s: %s".formatted(e.getKey(), e.getValue().name())));
    }
}