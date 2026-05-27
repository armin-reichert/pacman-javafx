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

    private final Keyboard keyboard;
    private final Map<KeyCodeCombination, GameAction> actionByKeyCombination = new HashMap<>();

    public GameActionBindingsManager(Keyboard keyboard) {
        this.keyboard = requireNonNull(keyboard);
    }

    @Override
    public void dispose() {
        actionByKeyCombination.clear();
    }

    @Override
    public Map<KeyCodeCombination, GameAction> keyCombinationToActionMap() {
        return actionByKeyCombination;
    }

    @Override
    public boolean noBindings() {
        return actionByKeyCombination.isEmpty();
    }

    @Override
    public void assignToKeyboard() {
        for (KeyCodeCombination combination : actionByKeyCombination.keySet()) {
            keyboard.registerActionBinding(combination, this);
        }
        logBindings();
        Logger.info("Key bindings assigned");
    }

    @Override
    public void removeFromKeyboard() {
        for (KeyCodeCombination combination : actionByKeyCombination.keySet()) {
            keyboard.unregisterActionBinding(combination, this);
        }
        Logger.info("Key bindings removed");
    }

    @Override
    public void add(GameAction action, KeyCodeCombination combination) {
        requireNonNull(action);
        requireNonNull(combination);
        actionByKeyCombination.put(combination, action);
    }

    @Override
    public void addAny(GameAction gameAction, Set<ActionBinding> bindings) {
        requireNonNull(gameAction);
        requireNonNull(bindings);
        bindings.stream()
            .filter(actionBinding -> actionBinding.gameAction() == gameAction)
            .findAny()
            .ifPresent(this::registerBinding);
    }

    @Override
    public void addAll(Set<ActionBinding> bindings) {
        for (ActionBinding binding : bindings) {
            registerBinding(binding);
        }
    }

    @Override
    public Optional<GameAction> matchingAction() {
        return actionByKeyCombination.keySet().stream()
            .filter(keyboard::isMatching)
            .map(actionByKeyCombination::get)
            .findFirst();
    }

    private void registerBinding(ActionBinding binding) {
        for (KeyCodeCombination combination : binding.keyCombinations()) {
            actionByKeyCombination.put(combination, binding.gameAction());
        }
    }

    private void logBindings() {
        // Sort output by key combination display text
        actionByKeyCombination.entrySet().stream()
            .sorted(Comparator.comparing(e -> e.getKey().toString()))
            .forEach(e -> Logger.debug("%-20s: %s".formatted(e.getKey(), e.getValue().resourceBundleKey())));
    }
}