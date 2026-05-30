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
    public void setKeyCombinationFor(GameAction action, KeyCodeCombination combination) {
        requireNonNull(action);
        requireNonNull(combination);
        actionForKeyCombination.put(combination, action);
    }

    @Override
    public void registerBindingFromSet(GameAction gameAction, Set<ActionBinding> bindings) {
        requireNonNull(gameAction);
        requireNonNull(bindings);

        final List<ActionBinding> matchingBindings = bindings.stream()
            .filter(actionBinding -> actionBinding.gameAction() == gameAction)
            .toList();

        final int count = matchingBindings.size();
        if (count == 0) {
            return;
        }
        if (count > 1) {
            Logger.warn("Found {} bindings for action {}, selecting first one", count, gameAction);
        }
        registerBinding(matchingBindings.getFirst());
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

    @Override
    public void logBindings() {
        // Sort output by key combination display text
        actionForKeyCombination.entrySet().stream()
            .sorted(Comparator.comparing(e -> e.getKey().toString()))
            .forEach(e -> Logger.info("%-20s: %s".formatted(e.getKey(), e.getValue().resourceBundleKey())));
    }

    private void registerBinding(ActionBinding binding) {
        requireNonNull(binding);
        for (KeyCodeCombination combination : binding.keyCombinations()) {
            actionForKeyCombination.put(combination, binding.gameAction());
        }
    }
}